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
        UserData user = userDataRepository.findByEmail(principal.getName());
        List<UserData> myFamily = getMyFamily(user);

        model.addAttribute("allEvents", eventToJsonService.getAllEventsToUser(myFamily, user));
        model.addAttribute("familyEvents", eventToJsonService.getAllEventsToUsersFamilyMembers(myFamily, user));
        model.addAttribute("UserRoles", userRoleRepository.findAllByUserIdAndMembershipTypeIsNot(user.getId(), UserPermission.VISITOR));
        model.addAttribute("msg", msg);
        return "mainPage";
    }

    @GetMapping("/family")
    public String getFamilyPage(Model model, Principal principal,
                                @ModelAttribute("errors") String errors) {
        if (userDataRepository.findByEmail(principal.getName()) != null) {
            UserData user = userDataRepository.findByEmail(principal.getName());
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
        UserDataValidator validation = new UserDataValidator(userDataRepository, principal);
        if (validation.supports(user.getClass())) {
            validation.validate(user, br);
        }
        UserData updateUser = userDataRepository.findByEmail(principal.getName());
        if (br.hasErrors() || !passwordEncoder.passwordEncoder().matches(password, updateUser.getPassword())) {
            if (!passwordEncoder.passwordEncoder().matches(password, updateUser.getPassword())) {
                br.addError(new ObjectError("user", "Password didn't match"));
            }
            redirectAttributes.addFlashAttribute("errors", getErrorMessages(br));
            return "redirect:/family";
        } else {
            updateUser(user, updateUser);
        }
        return "redirect:/family";
    }

    @RequestMapping(value = "/family/{memberId}", method = RequestMethod.POST)
    public String acceptOrDeleteFamilyMember(@PathVariable Integer memberId, @RequestParam boolean value,
                                             Principal principal) {
        if (userDataRepository.findByEmail(principal.getName()) != null) {
            UserData user = userDataRepository.findByEmail(principal.getName());
            if (userDataRepository.findById(memberId).isPresent()) {
                UserData member = userDataRepository.findById(memberId).get();
                FamilyData familyData = familyDataRepository.findByFamilyId(getFamilyId(user, member));


                /*
                * Accepted familyMembers will be set to admins for users private board.
                * */
                if (value) {
                    familyData.setAreFamily(true);
                    familyData.setPendingRequest(false);
                    familyDataRepository.save(familyData);
                    if (boardDataRepository.findById(user.getMyBoardId()).isPresent()){
                        BoardData privateBoard = boardDataRepository.findById(user.getMyBoardId()).get();
                        userRoleRepository.save(new UserRole(member, privateBoard, UserPermission.ADMIN, false));
                    }
                } else {
                    familyDataRepository.delete(familyData);
                }
            }
        }
        return "redirect:/family";
    }

    @RequestMapping(value = "/familyMember", method = RequestMethod.POST)
    public String sendFamilyMemberRequestByMail(@RequestParam String sendTo, Principal principal,
                                                RedirectAttributes ra) {
        if (sendTo.matches("^(.+)@(.+)$")) {
            UserData sender = userDataRepository.findByEmail(principal.getName());
            if (userDataRepository.findByEmail(sendTo) != null) {
                UserData isAlreadyUser = userDataRepository.findByEmail(sendTo);
                familyRequestRepository.save(new FamilyRequest(sender, sendTo, true));
                familyDataRepository.save(new FamilyData(sender, isAlreadyUser, true, false, sender));
            } else {
                emailService.sendFamilyMemberRequest(sendTo, principal);
                familyRequestRepository.save(new FamilyRequest(sender, sendTo, false));
            }
        } else {
            ra.addFlashAttribute("errors", "Not a valid Email");
        }
        return "redirect:/family";
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
            if (fd.isPendingRequest() && fd.getRequestedBy() != user){
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
            if(fd.isAreFamily()){
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
