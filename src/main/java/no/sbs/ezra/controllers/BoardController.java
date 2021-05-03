package no.sbs.ezra.controllers;

import lombok.AllArgsConstructor;
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
import no.sbs.ezra.servises.EmailService;
import no.sbs.ezra.servises.PermissionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import static no.sbs.ezra.constants.Constants.*;


@Controller
@AllArgsConstructor
public class BoardController {

    private final UserDataRepository userDataRepository;
    private final BoardDataRepository boardDataRepository;
    private final UserRoleRepository userRoleRepository;
    private final EventDataRepository eventDataRepository;
    private final PermissionService permissionService;
    private final EmailService emailService;


    @GetMapping("/board/{boardId}")
    public String getBoardPage(Model model, @PathVariable Integer boardId, Principal principal) {
        if (boardDataRepository.findById(boardId).isPresent()) {
            UserData user = userDataRepository.findByEmail(principal.getName());
            BoardData board = boardDataRepository.findById(boardId).get();
            UserRole userRole = userRoleRepository.findByBoardIdAndUserId(board.getId(), user.getId());
            handleFirstTimeVisitor(board, user);
            if (board.isPrivateBoard() && userRole.getMembershipType() == UserPermission.VISITOR) return HOME_PAGE;
            model.addAttribute("userPermission", userRoleRepository.findByBoardIdAndUserId(boardId, user.getId()));
            model.addAttribute("board", board);
            try {
                model.addAttribute("events", permissionService.getAllEventsFromBoardByUserPermission(board, userRole.getMembershipType()));
            } catch (NullPointerException ignore){}
            return "boardPage";
        }
        return HOME_PAGE;
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
                model.addAttribute("userRole", role);
                model.addAttribute("eventData", getNewOrExistingEvent(eventId));
                return "createOrEditEventPage";
            }
        }
        return HOME_PAGE;
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
                model.addAttribute("permissions", userPermissionsInteraction(role.getMembershipType()));
                return "boardMembersPage";
            }
        }
        return HOME_PAGE;
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
        return HOME_PAGE;
    }

    @GetMapping("/createBoard")
    public String getCreateBoardPage(Model model, Principal principal) {
        model.addAttribute("user", userDataRepository.findByEmail(principal.getName()));
        model.addAttribute("BoardData", new BoardData());
        model.addAttribute("errors", new ArrayList<>());
        return "createNewBoardPage";
    }


    /*
    * When notification gets implemented this need to be fixed !!
    * --For now only sends invite if sentTO is not a user jet or never have visited the board.
    * If Spam becomes a problem, make it so the invite only sends to created users.
    * */
    @RequestMapping(value = "/board/{boardId}/sendInvite")
    public String sendInviteByMail(@PathVariable Integer boardId, @RequestParam String sendTo, Principal principal){
        if (boardDataRepository.findById(boardId).isPresent()){
            BoardData board = boardDataRepository.findById(boardId).get();
            if (sendTo.matches("^(.+)@(.+)$")){
                if (userDataRepository.findByEmail(sendTo) != null){
                    UserData sender = userDataRepository.findByEmail(principal.getName());
                    UserData invitedUser = userDataRepository.findByEmail(sendTo);
                    if (boardId == sender.getMyBoardId()){
                        /*
                        * This makes it possible for a user to invite another user to their private board.
                        * Will also make it possible to keep a board private and for the master to invite other users.
                        * */
                        if (userRoleRepository.findByBoardIdAndUserId(board.getId(), invitedUser.getId()) == null){
                            userRoleRepository.save(new UserRole(invitedUser, board, UserPermission.FOLLOWER, false));
                        }
                    } else {
                        if (userRoleRepository.findByBoardIdAndUserId(board.getId(), invitedUser.getId()) == null){
                            emailService.sendEmailInviteToBoard(sendTo, principal, board);
                        }
                    }
                }
                //Remove this else if spam becomes a problem.
                else {
                    emailService.sendEmailInviteToBoard(sendTo, principal, board);
                }
            }
        }
        return "redirect:/board/" + boardId;
    }

    @RequestMapping(value = "/createBoard", method = RequestMethod.POST)
    public String CreateNewBoard(@Valid @ModelAttribute("BoardData") BoardData boardData, BindingResult br,
                                 Principal principal, Model model,
                                 @RequestParam( name = "isPrivate", required = false) boolean isPrivate) {
        BoardDataValidator validator = new BoardDataValidator();
        model.addAttribute("user", userDataRepository.findByEmail(principal.getName()));
        if (validator.supports(boardData.getClass())) {
            validator.validate(boardData, br);
            if (br.hasErrors()) {
                model.addAttribute("errors", getErrorMessages(br));
                return "createNewBoardPage";
            } else {
                if (isPrivate){
                    boardData.setPrivateBoard(true);
                }
                boardDataRepository.save(boardData);
                userRoleRepository.save(new UserRole(userDataRepository.findByEmail(principal.getName()), boardData, UserPermission.MASTER, false));
            }
        }
        return HOME_PAGE;
    }

    @RequestMapping(value = "/board/{boardId}/edit", method = RequestMethod.POST)
    public String editOrDeleteBoard(@PathVariable Integer boardId, @RequestParam(name = "value", required = false) String value,
                                    @ModelAttribute("BoardData") BoardData boardData, BindingResult br, Model model,
                                    RedirectAttributes redirectAttributes, @RequestParam( name = "isPrivate", required = false, defaultValue = "false") boolean isPrivate) {
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
                            redirectAttributes.addFlashAttribute("message","Board successfully deleted!");
                            return HOME_PAGE;
                        }
                    } else {
                        updateBoardInformation(boardId, boardData, isPrivate);
                        return "redirect:/board/" + boardId;
                    }
                }
            }
        }
        return HOME_PAGE;
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
                return String.format("redirect:/board/%d/members", boardId);
            }
        }
        return HOME_PAGE;
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
                        model.addAttribute("userRole", role);
                        return "createOrEditEventPage";
                    } else {
                        saveOrUpdateEvent(eventData, eventId);
                        return HOME_PAGE;
                    }
                }
            }
        }
        return HOME_PAGE;
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
        return HOME_PAGE;
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


    /*
    * This method let user follow, request membership and withdraw request to a board
    * */
    private void handleMembershipRequest(UserRole role) {
        switch (role.getMembershipType().getPermission()) {
            case "follower" -> role.setPendingMember(!role.isPendingMember());
            case "visitor" -> role.setMembershipType(UserPermission.FOLLOWER);
        }
        userRoleRepository.save(role);
    }

    /*
    * This method let a Admin/Master handle members permission.
    * Setting a member to FOLLOWER is the same as kicking them out.
    *  */
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

    /*
    * Every user needs to have a UserRole on a board,
    * this sets user to visitor if no UserRole is found.
    * */
    private void handleFirstTimeVisitor(BoardData board, UserData user) {
        if (userRoleRepository.findByBoardIdAndUserId(board.getId(), user.getId()) == null) {
            userRoleRepository.save(new UserRole(user, board, UserPermission.VISITOR, false));
        }
    }

    /*
    * provides a list of UserPermissions based on the users UserRole for handleBoardMembersPermission()
    *  */
    public List<UserPermission> userPermissionsInteraction(UserPermission permission) {
        List<UserPermission> temp = new ArrayList<>();
        if (permission.getPermission().equals("master")){
            temp.add(UserPermission.ADMIN);
        }
        temp.add(UserPermission.MEMBER);
        temp.add(UserPermission.FOLLOWER);
        if (permission.getPermission().equals("master")){
            temp.add(UserPermission.VISITOR);
        }
        return temp;
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
    private void updateBoardInformation(Integer boardId, BoardData boardData, boolean isPrivate) {
        if (boardDataRepository.findById(boardId).isPresent()){
            BoardData update = boardDataRepository.findById(boardId).get();
            update.setPrivateBoard(isPrivate);
            update.setContactEmail(boardData.getContactEmail());
            update.setContactNumber(boardData.getContactNumber());
            update.setContactName(boardData.getContactName());
            update.setHomepage(boardData.getHomepage());
            update.setName(boardData.getName());
            boardDataRepository.save(update);
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
}
