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
import no.sbs.ezra.security.PasswordConfig;
import no.sbs.ezra.security.UserPermission;
import org.apache.catalina.User;
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
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

@Controller
public class BoardController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PasswordConfig passwordEncoder;
    private final UserDataRepository userDataRepository;
    private final BoardDataRepository boardDataRepository;
    private final UserRoleRepository userRoleRepository;
    private final EventDataRepository eventDataRepository;


    public BoardController(PasswordConfig passwordEncoder, UserDataRepository userDataRepository, BoardDataRepository boardDataRepository, UserRoleRepository userRoleRepository, EventDataRepository eventDataRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userDataRepository = userDataRepository;
        this.boardDataRepository = boardDataRepository;
        this.userRoleRepository = userRoleRepository;
        this.eventDataRepository = eventDataRepository;
    }

    @GetMapping("/board/{id}")
    public String getBoardPage(Model model, @PathVariable Integer id, Principal principal){
        if (id > 0){
            UserData user = userDataRepository.findByEmail(principal.getName());
            if (id.toString().matches("^[0-9]*$")){
                BoardData board;
                try{
                    board = boardDataRepository.findById(id).get();
                } catch (Exception e){
                    logger.error("Cant find boardData, id does not exist");
                    return "redirect:/";
                }
                model.addAttribute("userPermission", userRoleRepository.findByBoardIdAndUserId(id, user.getId()).getMembershipType());
                model.addAttribute("events", eventDataRepository.findAllByBoardIdAndAndMembershipType(id, userRoleRepository.findByBoardIdAndUserId(id, user.getId()).getMembershipType()));
                model.addAttribute("board", board);
                return "boardPage";
            }
        }
        return "redirect:/";
    }

    @GetMapping(value = {"/createEvent/{boardId}", "/createEvent/{boardId}/{eventId}"})
    public String getCreateOrEditEventPage(Model model, @PathVariable Integer boardId,
                                           @RequestParam( name = "eventId", required = false) Integer eventId, Principal principal){

        UserData user = userDataRepository.findByEmail(principal.getName());
        BoardData board = getBoardData(boardId);
        if (board != null){
            UserRole role = userRoleRepository.findByBoardIdAndUserId(board.getId(), user.getId());
            if (role.getMembershipType().getPermission().equals("master")){
                EventData event;
                if (eventId != null){
                    if (eventDataRepository.findById(eventId).isPresent()){
                        event = eventDataRepository.findById(eventId).get();
                    } else {
                        event = new EventData();
                    }
                } else {
                    event = new EventData();
                }
                model.addAttribute("board", board);
                model.addAttribute("eventData", event);
                return "createOrEditEventPage";
            }else {
                logger.error("User with id: " + user.getId() + " attempted access to /createEvent/" + boardId + ", but dont have permission");
                return "redirect:/board/" + boardId;
            }
        } else {
            logger.error("BordId: "+ boardId + ", does not exist");
            return "redirect:/";
        }
    }

    private EventData getEventDataOrFillWithEmptyEventData(Integer eventId) {
        EventData event;
        if (eventDataRepository.findById(eventId).isPresent()){
            event = eventDataRepository.findById(eventId).get();
        } else {
            event = new EventData();
        }
        return event;
    }

    @RequestMapping(value = "/createEvent/{boardId}", method = RequestMethod.POST)
    public String createOrEditEvent(@Valid @ModelAttribute("eventData") EventData eventData, BindingResult br,
                                    @PathVariable Integer boardId, Principal principal, Model model){
        UserData user = userDataRepository.findByEmail(principal.getName());
        BoardData board = getBoardData(boardId);
        if (board == null) return "redirect:/";
        eventData.setBoard(board);
        UserRole role = userRoleRepository.findByBoardIdAndUserId(board.getId(), user.getId());
        if (role.getMembershipType().getPermission().equals("master")){
            EventDataValidator validator = new EventDataValidator(eventDataRepository);
            if (validator.supports(eventData.getClass())) {
                validator.validate(eventData, br);
            } else {
                logger.error("Failed to support EventData class and or validate EventData");
            }
            if (br.hasErrors()) {
                System.out.println(eventData);
                model.addAttribute("errors", getAllValidationErrorsForEventData(br));
                return "createOrEditEventPage";
            } else {
                System.out.println(eventData);
                saveOrUpdateEvent(eventData);
                return "redirect:/";
            }
        } else {
            return "redirect:/";
        }
    }

    private BoardData getBoardData(Integer boardId){
        BoardData board;
        try{
            board = boardDataRepository.findById(boardId).orElseThrow();
        } catch (NoSuchElementException e){
            logger.error("Failed to get BoardData\n"+e.getLocalizedMessage());
            return null;
        }
        return board;
    }

    private void saveOrUpdateEvent(EventData event) {
        if (eventDataRepository.findById(event.getId()).isPresent()){
            EventData existingEvent = eventDataRepository.findById(event.getId()).get();
            eventDataRepository.save(existingEvent);
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
        logger.error("EventData validation errors: "+errors.toString());
        return errors;
    }
}
