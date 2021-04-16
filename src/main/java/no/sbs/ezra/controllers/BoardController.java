package no.sbs.ezra.controllers;

import no.sbs.ezra.data.BoardData;
import no.sbs.ezra.data.EventData;
import no.sbs.ezra.data.UserData;
import no.sbs.ezra.data.UserRole;
import no.sbs.ezra.data.repositories.BoardDataRepository;
import no.sbs.ezra.data.repositories.EventDataRepository;
import no.sbs.ezra.data.repositories.UserDataRepository;
import no.sbs.ezra.data.repositories.UserRoleRepository;
import no.sbs.ezra.data.validators.EventDataValidator;
import no.sbs.ezra.security.UserPermission;
import no.sbs.ezra.servises.EventPermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static no.sbs.ezra.security.UserRoles.VISITOR;

@Controller
public class BoardController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserDataRepository userDataRepository;
    private final BoardDataRepository boardDataRepository;
    private final UserRoleRepository userRoleRepository;
    private final EventDataRepository eventDataRepository;
    private final EventPermissionService eventPermissionService;


    public BoardController(UserDataRepository userDataRepository, BoardDataRepository boardDataRepository,
                           UserRoleRepository userRoleRepository, EventDataRepository eventDataRepository,
                           EventPermissionService eventPermissionService) {
        this.userDataRepository = userDataRepository;
        this.boardDataRepository = boardDataRepository;
        this.userRoleRepository = userRoleRepository;
        this.eventDataRepository = eventDataRepository;
        this.eventPermissionService = eventPermissionService;
    }

    @GetMapping("/board/{id}")
    public String getBoardPage(Model model, @PathVariable Integer id, Principal principal) {
        if (id > 0) {
            if (id.toString().matches("^[0-9]*$")) {
                if (boardDataRepository.findById(id).isPresent()) {
                    UserData user = userDataRepository.findByEmail(principal.getName());
                    try{
                        userRoleRepository.findByBoardIdAndUserId(id, user.getId()).getMembershipType();
                    } catch (NullPointerException ignore){
                        userRoleRepository.save(new UserRole(user.getId(), id, UserPermission.VISITOR, false));
                    }
                    model.addAttribute("userPermission", userRoleRepository.findByBoardIdAndUserId(id, user.getId()).getMembershipType());
                    model.addAttribute("events", eventPermissionService.getAllEventsFromBoardByUserPermission(boardDataRepository.findById(id).get(), userRoleRepository.findByBoardIdAndUserId(id, user.getId()).getMembershipType()));
                    model.addAttribute("board", boardDataRepository.findById(id).get());
                    return "boardPage";
                } else {
                    logger.error("Cant find boardData with id: " + id + ", id does not exist");
                }
            }
        }
        return "redirect:/";
    }

    @GetMapping(value = {"/createEvent/{boardId}", "/createEvent/{boardId}/{eventId}"})
    public String getCreateOrEditEventPage(Model model, @PathVariable Integer boardId,
                                           @PathVariable(required = false) Integer eventId, Principal principal) {
        BoardData board = getBoardData(boardId);
        if (board != null) {
            UserData user = userDataRepository.findByEmail(principal.getName());
            UserRole role = userRoleRepository.findByBoardIdAndUserId(board.getId(), user.getId());
            if (role.getMembershipType().getPermission().equals("master")) {
                model.addAttribute("board", board);
                model.addAttribute("eventData", getNewOrExistingEvent(eventId));
                return "createOrEditEventPage";
            } else {
                logger.error("User with id: " + user.getId() + " attempted access to /createEvent/" + boardId + ", but dont have permission");
                return "redirect:/board/" + boardId;
            }
        } else {
            logger.error("BordId: " + boardId + ", does not exist");
            return "redirect:/";
        }
    }

    @RequestMapping(value = {"/createEvent/{boardId}", "/createEvent/{boardId}/{eventId}" }, method = RequestMethod.POST)
    public String createOrEditEvent(@Valid @ModelAttribute("eventData") EventData eventData, BindingResult br,
                                    @PathVariable Integer boardId, @PathVariable(required = false) Integer eventId,
                                    Principal principal, Model model) {
        UserData user = userDataRepository.findByEmail(principal.getName());
        BoardData board = getBoardData(boardId);
        if (board == null) return "redirect:/";
        eventData.setBoard(board);
        UserRole role = userRoleRepository.findByBoardIdAndUserId(board.getId(), user.getId());
        if (role.getMembershipType().getPermission().equals("master")) {
            EventDataValidator validator = new EventDataValidator();
            if (validator.supports(eventData.getClass())) {
                validator.validate(eventData, br);
            } else {
                logger.error("Failed to support EventData class and or validate EventData");
            }
            if (br.hasErrors()) {
                model.addAttribute("errors", getAllValidationErrorsForEventData(br));
                model.addAttribute("board", board);
                return "createOrEditEventPage";
            } else {
                saveOrUpdateEvent(eventData, eventId);
                return "redirect:/";
            }
        } else {
            return "redirect:/";
        }
    }

    @RequestMapping(value = "/deleteEvent/{boardId}/{eventId}", method = RequestMethod.POST)
    public String deleteEvent(@PathVariable Integer boardId, @PathVariable Integer eventId, Principal principal){
        UserData user = userDataRepository.findByEmail(principal.getName());
        BoardData board = getBoardData(boardId);
        if (board == null) return "redirect:/";
        UserRole role = userRoleRepository.findByBoardIdAndUserId(board.getId(), user.getId());
        if (role.getMembershipType().getPermission().equals("master") || role.getMembershipType().getPermission().equals("admin")) {
            if (eventDataRepository.findById(eventId).isPresent()){
                logger.info("Event: " + eventDataRepository.findById(eventId).get() + ", was deleted by user with id: " + user.getId());
                eventDataRepository.delete(eventDataRepository.findById(eventId).get());
            }
        }
        return "redirect:/board/" + boardId;
    }

    @RequestMapping(value = "/board/{boardId}/access", method = RequestMethod.POST)
    public String increaseMembership(@PathVariable Integer boardId, Principal principal){
        UserData user = userDataRepository.findByEmail(principal.getName());
        BoardData board;
        if (boardDataRepository.findById(boardId).isPresent()) {
            board = boardDataRepository.findById(boardId).get();
        } else {
            return "redirect:/";
        }
        UserRole ur = userRoleRepository.findByBoardIdAndUserId(board.getId(), user.getId());
        switch (ur.getMembershipType().getPermission()) {
            case "follower" -> {
                ur.setPendingMember(true);
                userRoleRepository.save(ur);
            }
            case "visitor" -> {
                ur.setMembershipType(UserPermission.FOLLOWER);
                userRoleRepository.save(ur);
                //todo: Add notification for successfully applies!
            }
            default -> {
                logger.error("Something went wrong while trying to increase membership");
                return "redirect:/";
            }
        }
        return "redirect:/board/" + boardId;
    }

    private EventData getNewOrExistingEvent(Integer eventId) {
        EventData event;
        if (eventId != null) {
            if (eventDataRepository.findById(eventId).isPresent()) {
                event = eventDataRepository.findById(eventId).get();
                return event;
            } else {
                event = new EventData();
            }
        } else {
            event = new EventData();
        }
        return event;
    }
    private BoardData getBoardData(Integer boardId) {
        BoardData board;
        if (boardDataRepository.findById(boardId).isPresent()) {
            board = boardDataRepository.findById(boardId).orElseThrow();
        } else {
            logger.error("Failed to get BoardData\n");
            return null;
        }
        return board;
    }
    private void saveOrUpdateEvent(EventData event, Integer eventId) {
        if (eventDataRepository.findById(eventId).isPresent()) {
            EventData update = eventDataRepository.findById(eventId).get();
            update.setBoard(event.getBoard());
            update.setMessage(event.getMessage());
            update.setMembershipType(event.getMembershipType());
            update.setLocation(event.getLocation());
            update.setEventName(event.getEventName());
            update.setDatetime_to(event.getDatetime_to());
            update.setDatetime_from(event.getDatetime_from());
            eventDataRepository.save(update);
            logger.info("Event updated: " + event.toString());
        } else {
            eventDataRepository.save(event);
            logger.info("New event created: " + event.toString());
        }
    }
    private List<String> getAllValidationErrorsForEventData(BindingResult br) {
        List<ObjectError> allErrors = br.getAllErrors();
        logger.error(br.getAllErrors().toString());
        List<String> errors = new ArrayList<>();
        for (ObjectError e :
                allErrors) {
            errors.add(e.getDefaultMessage());
        }
        logger.error("EventData validation errors: " + errors.toString());
        return errors;
    }
}
