package no.sbs.ezra.controllers;

import no.sbs.ezra.data.BoardData;
import no.sbs.ezra.data.EventData;
import no.sbs.ezra.data.UserData;
import no.sbs.ezra.data.UserRole;
import no.sbs.ezra.data.repositories.BoardDataRepository;
import no.sbs.ezra.data.repositories.EventDataRepository;
import no.sbs.ezra.data.repositories.UserDataRepository;
import no.sbs.ezra.data.repositories.UserRoleRepository;
import no.sbs.ezra.data.validators.BoardDataValidator;
import no.sbs.ezra.data.validators.EventDataValidator;
import no.sbs.ezra.security.UserPermission;
import no.sbs.ezra.servises.PermissionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class BoardController {

    private final UserDataRepository userDataRepository;
    private final BoardDataRepository boardDataRepository;
    private final UserRoleRepository userRoleRepository;
    private final EventDataRepository eventDataRepository;
    private final PermissionService permissionService;


    public BoardController(UserDataRepository userDataRepository, BoardDataRepository boardDataRepository,
                           UserRoleRepository userRoleRepository, EventDataRepository eventDataRepository,
                           PermissionService permissionService) {
        this.userDataRepository = userDataRepository;
        this.boardDataRepository = boardDataRepository;
        this.userRoleRepository = userRoleRepository;
        this.eventDataRepository = eventDataRepository;
        this.permissionService = permissionService;
    }

    @GetMapping("/board/{boardId}")
    public String getBoardPage(Model model, @PathVariable Integer boardId, Principal principal) {
        if (boardDataRepository.findById(boardId).isPresent()) {
            UserData user = userDataRepository.findByEmail(principal.getName());
            BoardData board = boardDataRepository.findById(boardId).get();
            UserRole userRole = userRoleRepository.findByBoardIdAndUserId(board.getId(), user.getId());

            handleFirstTimeVisitor(board, user);
            model.addAttribute("userPermission", userRoleRepository.findByBoardIdAndUserId(boardId, user.getId()));
            model.addAttribute("events", permissionService.getAllEventsFromBoardByUserPermission(board, userRole.getMembershipType()));
            model.addAttribute("board", board);
            return "boardPage";
        }
        return "redirect:/";
    }

    @GetMapping(value = {"/createEvent/{boardId}", "/createEvent/{boardId}/{eventId}"})
    public String getCreateOrEditEventPage(Model model, @PathVariable Integer boardId,
                                           @PathVariable(required = false) Integer eventId, Principal principal) {
        if (boardDataRepository.findById(boardId).isPresent()) {
            BoardData board = boardDataRepository.findById(boardId).get();
            UserData user = userDataRepository.findByEmail(principal.getName());
            UserRole role = userRoleRepository.findByBoardIdAndUserId(board.getId(), user.getId());
            if (permissionService.doesUserHaveAdminRights(role.getMembershipType())) {
                model.addAttribute("board", board);
                model.addAttribute("eventData", getNewOrExistingEvent(eventId));
                return "createOrEditEventPage";
            }
        }
        return "redirect:/";
    }

    @GetMapping("/board/{boardId}/members")
    public String getBoardMembersPage(Model model, @PathVariable Integer boardId, Principal principal) {
        if (boardDataRepository.findById(boardId).isPresent()) {
            BoardData board = boardDataRepository.findById(boardId).get();
            UserData user = userDataRepository.findByEmail(principal.getName());
            UserRole role = userRoleRepository.findByBoardIdAndUserId(board.getId(), user.getId());
            if (permissionService.doesUserHaveAdminRights(role.getMembershipType())) {
                model.addAttribute("members", userRoleRepository.findAllByBoardIdAndMembershipTypeOrMembershipType(board.getId(), UserPermission.MEMBER, UserPermission.ADMIN));
                model.addAttribute("pendingMembers", userRoleRepository.findAllByBoardIdAndMembershipTypeAndPendingMember(board.getId(), UserPermission.FOLLOWER, true));
                model.addAttribute("board", board);
                model.addAttribute("permissions", userPermissionsInteraction());
                return "boardMembersPage";
            }
        }
        return "redirect:/";
    }

    @GetMapping("/board/{boardId}/edit")
    public String getEditBoardPage(Model model, @PathVariable Integer boardId, Principal principal) {
        if (boardDataRepository.findById(boardId).isPresent()) {
            BoardData board = boardDataRepository.findById(boardId).get();
            UserData user = userDataRepository.findByEmail(principal.getName());
            UserRole role = userRoleRepository.findByBoardIdAndUserId(board.getId(), user.getId());
            if (role.getMembershipType() == UserPermission.MASTER) {
                model.addAttribute("board", boardDataRepository.findById(boardId).get());
                model.addAttribute("errors", new ArrayList<>());
                return "editBoardPage";
            }
        }
        return "redirect:/";
    }

    @GetMapping("/createBoard")
    public String getCreateBoardPage(Model model, Principal principal) {
        model.addAttribute("user", userDataRepository.findByEmail(principal.getName()));
        model.addAttribute("BoardData", new BoardData());
        model.addAttribute("errors", new ArrayList<>());
        return "createNewBoardPage";
    }

    @RequestMapping(value = "/createBoard", method = RequestMethod.POST)
    public String CreateNewBoard(@Valid @ModelAttribute("BoardData") BoardData boardData, BindingResult br,
                                 Principal principal, Model model) {
        BoardDataValidator validator = new BoardDataValidator();
        model.addAttribute("user", userDataRepository.findByEmail(principal.getName()));
        if (validator.supports(boardData.getClass())) {
            validator.validate(boardData, br);
            if (br.hasErrors()) {
                model.addAttribute("errors", getErrorMessages(br));
                return "createNewBoardPage";
            } else {
                boardDataRepository.save(boardData);
                userRoleRepository.save(new UserRole(userDataRepository.findByEmail(principal.getName()), boardData, UserPermission.MASTER, false));
            }
        }
        return "redirect:/";
    }

    @RequestMapping(value = "/board/{boardId}/edit", method = RequestMethod.POST)
    public String editOrDeleteBoard(@PathVariable Integer boardId, @RequestParam(name = "value", required = false) String value,
                                    @ModelAttribute("BoardData") BoardData boardData, BindingResult br, Model model) {
        if (boardDataRepository.findById(boardId).isPresent()) {
            BoardDataValidator validator = new BoardDataValidator();
            if (validator.supports(boardData.getClass())) {
                validator.validate(boardData, br);
                if (br.hasErrors()) {
                    model.addAttribute("errors", getErrorMessages(br));
                    model.addAttribute("board", boardDataRepository.findById(boardId).get());
                    System.out.println(br.getAllErrors());
                    return "editBoardPage";
                } else {
                    if (value.length() == 6) {
                        if (value.equals("DELETE")) {
                            boardDataRepository.delete(boardDataRepository.findById(boardId).get());
                        }
                    } else {
                        updateBoardInformation(boardId, boardData);
                        return "redirect:/board/" + boardId;
                    }
                }
            }
        }
        return "redirect:/";
    }

    private void updateBoardInformation(Integer boardId, BoardData boardData) {
        if (boardDataRepository.findById(boardId).isPresent()){
            BoardData update = boardDataRepository.findById(boardId).get();
            update.setContactEmail(boardData.getContactEmail());
            update.setContactNumber(boardData.getContactNumber());
            update.setContactName(boardData.getContactName());
            update.setHomepage(boardData.getHomepage());
            update.setName(boardData.getName());
            boardDataRepository.save(update);
        }
    }

    @RequestMapping(value = "/board/{boardId}/members/{memberId}", method = RequestMethod.POST)
    public String handleBoardMembersPermission(@PathVariable Integer boardId, @RequestParam(name = "value") String value,
                                               @PathVariable Integer memberId, Principal principal) {
        if (boardDataRepository.findById(boardId).isPresent()) {
            if (userDataRepository.findById(memberId).isPresent()) {
                UserData loggedInUser = userDataRepository.findByEmail(principal.getName());
                BoardData board = boardDataRepository.findById(boardId).get();
                UserData member = userDataRepository.findById(memberId).get();
                UserRole memberUr = userRoleRepository.findByBoardIdAndUserId(board.getId(), member.getId());
                updateBoardMemberPermission(boardId, value, loggedInUser, memberUr);
            }
        }
        return "redirect:/";
    }


    @RequestMapping(value = {"/createEvent/{boardId}", "/createEvent/{boardId}/{eventId}"}, method = RequestMethod.POST)
    public String createOrEditEvent(@Valid @ModelAttribute("eventData") EventData eventData, BindingResult br,
                                    @PathVariable Integer boardId, @PathVariable(required = false) Integer eventId,
                                    Principal principal, Model model) {
        if (boardDataRepository.findById(boardId).isPresent()) {
            UserData user = userDataRepository.findByEmail(principal.getName());
            BoardData board = boardDataRepository.findById(boardId).get();
            UserRole role = userRoleRepository.findByBoardIdAndUserId(board.getId(), user.getId());
            eventData.setBoard(board);
            if (permissionService.doesUserHaveAdminRights(role.getMembershipType())) {
                EventDataValidator validator = new EventDataValidator();
                if (validator.supports(eventData.getClass())) {
                    validator.validate(eventData, br);
                    if (br.hasErrors()) {
                        model.addAttribute("errors", getErrorMessages(br));
                        model.addAttribute("board", board);
                        return "createOrEditEventPage";
                    } else {
                        saveOrUpdateEvent(eventData, eventId);
                        return "redirect:/";
                    }
                }
            }
        }
        return "redirect:/";
    }

    @RequestMapping(value = "/deleteEvent/{boardId}/{eventId}", method = RequestMethod.POST)
    public String deleteEvent(@PathVariable Integer boardId, @PathVariable Integer eventId, Principal principal) {
        if (boardDataRepository.findById(boardId).isPresent()) {
            UserData user = userDataRepository.findByEmail(principal.getName());
            BoardData board = boardDataRepository.findById(boardId).get();
            UserRole role = userRoleRepository.findByBoardIdAndUserId(board.getId(), user.getId());
            if (permissionService.doesUserHaveAdminRights(role.getMembershipType())) {
                if (eventDataRepository.findById(eventId).isPresent()) {
                    eventDataRepository.delete(eventDataRepository.findById(eventId).get());
                    return "redirect:/board/" + boardId;
                }
            }
        }
        return "redirect:/";
    }

    @RequestMapping(value = "/board/{boardId}/access", method = RequestMethod.POST)
    public String handleBoardMembershipRequests(@PathVariable Integer boardId, Principal principal) {
        if (boardDataRepository.findById(boardId).isPresent()) {
            UserData user = userDataRepository.findByEmail(principal.getName());
            BoardData board = boardDataRepository.findById(boardId).get();
            UserRole role = userRoleRepository.findByBoardIdAndUserId(board.getId(), user.getId());
            handleMembershipRequest(role);
        }
        return "redirect:/board/" + boardId;
    }

    @RequestMapping(value = "/board/{boardId}/unfollow", method = RequestMethod.POST)
    public String unfollowBoard(@PathVariable Integer boardId, Principal principal) {
        if (boardDataRepository.findById(boardId).isPresent()) {
            BoardData board = boardDataRepository.findById(boardId).get();
            UserData user = userDataRepository.findByEmail(principal.getName());
            UserRole ur = userRoleRepository.findByBoardIdAndUserId(board.getId(), user.getId());
            ur.setMembershipType(UserPermission.VISITOR);
            ur.setPendingMember(false);
            userRoleRepository.save(ur);
        }
        return "redirect:/board/" + boardId;
    }


    private void handleMembershipRequest(UserRole role) {
        switch (role.getMembershipType().getPermission()) {
            case "follower" -> role.setPendingMember(!role.isPendingMember());
            case "visitor" -> role.setMembershipType(UserPermission.FOLLOWER);
        }
        userRoleRepository.save(role);
    }

    private void updateBoardMemberPermission(Integer boardId, String value, UserData loggedInUser, UserRole memberUr) {
        switch (value) {
            case "ADMIN", "MEMBER" -> {
                if (permissionService.doesUserHaveAdminRights(userRoleRepository.findByBoardIdAndUserId(boardId, loggedInUser.getId()).getMembershipType())) {
                    if (value.equals("ADMIN")) {
                        memberUr.setMembershipType(UserPermission.ADMIN);
                    }
                    if (value.equals("MEMBER")) {
                        memberUr.setMembershipType(UserPermission.MEMBER);
                    }
                }
            }
            case "VISITOR" -> memberUr.setMembershipType(UserPermission.VISITOR);
            default -> memberUr.setMembershipType(UserPermission.FOLLOWER);
        }
        memberUr.setPendingMember(false);
        userRoleRepository.save(memberUr);
    }

    private void handleFirstTimeVisitor(BoardData board, UserData user) {
        if (userRoleRepository.findByBoardIdAndUserId(board.getId(), user.getId()) == null) {
            userRoleRepository.save(new UserRole(user, board, UserPermission.VISITOR, false));
        }
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
        } else {
            eventDataRepository.save(event);
        }
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

    private List<String> getErrorMessages(BindingResult br) {
        List<ObjectError> allErrors = br.getAllErrors();
        List<String> errors = new ArrayList<>();
        for (ObjectError e :
                allErrors) {
            errors.add(e.getDefaultMessage());
        }
        return errors;
    }

    public List<UserPermission> userPermissionsInteraction() {
        List<UserPermission> temp = new ArrayList<>();
        temp.add(UserPermission.MEMBER);
        temp.add(UserPermission.FOLLOWER);
        temp.add(UserPermission.ADMIN);
        return temp;
    }
}
