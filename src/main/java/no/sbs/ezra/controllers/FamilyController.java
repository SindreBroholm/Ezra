package no.sbs.ezra.controllers;

import lombok.AllArgsConstructor;
import no.sbs.ezra.data.*;
import no.sbs.ezra.data.repositories.*;
import no.sbs.ezra.data.validators.UserDataValidator;
import no.sbs.ezra.security.PasswordConfig;
import no.sbs.ezra.security.UserPermission;
import no.sbs.ezra.servises.EmailService;
import no.sbs.ezra.servises.EventToJsonService;
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

import static no.sbs.ezra.constants.Constants.FAMILY_PAGE;

@Controller
@AllArgsConstructor
public class FamilyController {

    private final UserDataRepository userDataRepository;
    private final FamilyDataRepository familyDataRepository;
    private final FamilyRequestRepository familyRequestRepository;
    private final EmailService emailService;
    private final EventToJsonService eventToJsonService;
    private final UserRoleRepository userRoleRepository;
    private final PasswordConfig passwordEncoder;
    private final BoardDataRepository boardDataRepository;

    @GetMapping("/")
    public String getMainPage(Model model, Principal principal, @ModelAttribute("message") String msg) {
        if (userDataRepository.findByEmail(principal.getName()).isPresent()) {
            UserData user = userDataRepository.findByEmail(principal.getName()).get();
            List<UserData> myFamily = getMyFamily(user);

            model.addAttribute("allEvents", eventToJsonService.getAllEventsToUser(myFamily, user));
            model.addAttribute("familyEvents", eventToJsonService.getAllEventsToUsersFamilyMembers(myFamily, user));
            model.addAttribute("UserRoles", userRoleRepository.findAllByUserIdAndMembershipTypeIsNot(user.getId(), UserPermission.VISITOR));
            model.addAttribute("msg", msg);
        }
        return "mainPage";
    }

    @GetMapping("/family")
    public String getFamilyPage(Model model, Principal principal,
                                @ModelAttribute("errors") String errors) {
        if (userDataRepository.findByEmail(principal.getName()).isPresent()) {
            UserData user = userDataRepository.findByEmail(principal.getName()).get();
            model.addAttribute("errors", errors.split(","));
            model.addAttribute("user", user);
            model.addAttribute("famPendingRequest", getPendingFamilyRequests(user));
            model.addAttribute("myFamily", getMyFamily(user));
        }
        return "familyPage";
    }

    @RequestMapping(value = "/family/editProfile")
    public String editProfile(@Valid @ModelAttribute("user") UserData user, BindingResult br,
                              @RequestParam(name = "value") String password, Principal principal,
                              RedirectAttributes redirectAttributes) {
        UserDataValidator validation = new UserDataValidator(userDataRepository, principal, passwordEncoder);
        if (validation.supports(user.getClass())) {
            validation.validate(user, br);
        }
        if (userDataRepository.findByEmail(principal.getName()).isPresent()) {
            UserData updateUser = userDataRepository.findByEmail(principal.getName()).get();
            if (br.hasErrors() || !passwordEncoder.passwordEncoder().matches(password, updateUser.getPassword())) {
                if (!passwordEncoder.passwordEncoder().matches(password, updateUser.getPassword())) {
                    br.addError(new ObjectError("user", "Password didn't match"));
                }
                redirectAttributes.addFlashAttribute("errors", getErrorMessages(br));
                return FAMILY_PAGE;
            } else {
                updateUser(user, updateUser);
            }
        }
        return FAMILY_PAGE;
    }

    @RequestMapping(value = "/family/{memberId}", method = RequestMethod.POST)
    public String acceptOrDeleteFamilyMember(@PathVariable Integer memberId, @RequestParam boolean value,
                                             Principal principal) {
        if (userDataRepository.findByEmail(principal.getName()).isPresent()) {
            UserData user = userDataRepository.findByEmail(principal.getName()).get();
            if (userDataRepository.findById(memberId).isPresent()) {
                UserData member = userDataRepository.findById(memberId).get();
                acceptOrDeleteFamilyRequest(value, user, member);
            }
        }
        return FAMILY_PAGE;
    }

    private void acceptOrDeleteFamilyRequest(boolean value, UserData user, UserData member) {
        FamilyData familyData = familyDataRepository.findByFamilyId(getFamilyId(user, member));
        if (value) {
            familyData.setAreFamily(true);
            familyData.setPendingRequest(false);
            familyDataRepository.save(familyData);
            setUserRoleToAdminForFamilyMembersPrivateBoard(user, member);
        }
        if (!value) {
            if (boardDataRepository.findById(member.getMyBoardId()).isPresent()) {
                BoardData memberBoard = boardDataRepository.findById(member.getMyBoardId()).get();
                UserRole userToMemberRole = userRoleRepository.findByBoardIdAndUserId(memberBoard.getId(), user.getId());
                UserRole memberToUserRole = userRoleRepository.findByBoardIdAndUserId(user.getMyBoardId(), member.getId());
                familyDataRepository.delete(familyData);
                userRoleRepository.delete(userToMemberRole);
                userRoleRepository.delete(memberToUserRole);
            }
        }
    }

    private void setUserRoleToAdminForFamilyMembersPrivateBoard(UserData user, UserData member) {
        if (boardDataRepository.findById(user.getMyBoardId()).isPresent()) {
            BoardData privateBoard = boardDataRepository.findById(user.getMyBoardId()).get();
            userRoleRepository.save(new UserRole(member, privateBoard, UserPermission.ADMIN, false));
            if (boardDataRepository.findById(member.getMyBoardId()).isPresent()) {
                BoardData famMemberPrivateBoard = boardDataRepository.findById(member.getMyBoardId()).get();
                userRoleRepository.save(new UserRole(user, famMemberPrivateBoard, UserPermission.ADMIN, false));
            }
        }
    }

    @RequestMapping(value = "/familyMember", method = RequestMethod.POST)
    public String sendFamilyMemberRequestByMail(@RequestParam String sendTo, Principal principal,
                                                RedirectAttributes ra) {
        if (sendTo.matches("^(.+)@(.+)$")) {
            if (userDataRepository.findByEmail(principal.getName()).isPresent()) {
                UserData sender = userDataRepository.findByEmail(principal.getName()).get();
                if (userDataRepository.findByEmail(sendTo).isPresent()) {
                    UserData isAlreadyUser = userDataRepository.findByEmail(sendTo).get();
                    familyRequestRepository.save(new FamilyRequest(sender, sendTo, true));
                    familyDataRepository.save(new FamilyData(sender, isAlreadyUser, true, false, sender));
                } else {
                    emailService.sendFamilyMemberRequest(sendTo, principal);
                    familyRequestRepository.save(new FamilyRequest(sender, sendTo, false));
                }
            } else {
                ra.addFlashAttribute("errors", "Not a valid Email");
            }
        }
        return FAMILY_PAGE;
    }

    private String getFamilyId(UserData user, UserData member) {
        /*
         * Since family_id is a String we need to split it and filter out witch id is user_id and witch one to search for.
         *  EKS: if user_id is 1 and family_id is  "1_13" we have to return the id of 13.
         *  Also we put the lowest id first. !Important
         * */
        String familyId;
        if (user.getId() > member.getId()) {
            familyId = member.getId() + "_" + user.getId();
        } else {
            familyId = user.getId() + "_" + member.getId();
        }
        return familyId;
    }

    private List<UserData> getPendingFamilyRequests(UserData user) {
        List<FamilyData> pendingFamilyRequests = familyDataRepository.addAllPendingFamilyRequests(user);
        List<UserData> temp = new ArrayList<>();
        for (FamilyData fd :
                pendingFamilyRequests) {
            if (fd.isPendingRequest() && fd.getRequestedBy() != user) {
                if (fd.getUserOne() == user) {
                    temp.add(fd.getUserTwo());
                } else {
                    temp.add(fd.getUserOne());
                }
            }
        }
        return temp;
    }

    private List<UserData> getMyFamily(UserData user) {
        List<FamilyData> famMembers = familyDataRepository.addAllFamilyMembersToUser(user);
        List<UserData> temp = new ArrayList<>();
        for (FamilyData fd :
                famMembers) {
            if (fd.isAreFamily()) {
                if (fd.getUserOne() == user) {
                    temp.add(fd.getUserTwo());
                } else {
                    temp.add(fd.getUserOne());
                }
            }
        }
        return temp;
    }

    private void updateUser(UserData user, UserData updateUser) {
        /*
         * There is no way for user to know if the password is accepted..
         * implement notification!
         * */
        if (user.getPassword().length() > 6 && user.getPassword().length() < 300) {
            updateUser.setEmail(user.getEmail());
            updateUser.setFirstname(user.getFirstname());
            updateUser.setLastname(user.getLastname());
            updateUser.setPhone_number(user.getPhone_number());
            updateUser.setPassword(passwordEncoder.passwordEncoder().encode(user.getPassword()));
            userDataRepository.save(updateUser);
        } else {
            updateUser.setEmail(user.getEmail());
            updateUser.setFirstname(user.getFirstname());
            updateUser.setLastname(user.getLastname());
            updateUser.setPhone_number(user.getPhone_number());
            userDataRepository.save(updateUser);
        }
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
