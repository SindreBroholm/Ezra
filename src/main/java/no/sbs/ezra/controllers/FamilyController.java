package no.sbs.ezra.controllers;

import lombok.AllArgsConstructor;
import no.sbs.ezra.data.FamilyData;
import no.sbs.ezra.data.FamilyRequest;
import no.sbs.ezra.data.UserData;
import no.sbs.ezra.data.repositories.FamilyDataRepository;
import no.sbs.ezra.data.repositories.FamilyRequestRepository;
import no.sbs.ezra.data.repositories.UserDataRepository;
import no.sbs.ezra.data.repositories.UserRoleRepository;
import no.sbs.ezra.security.UserPermission;
import no.sbs.ezra.servises.EmailService;
import no.sbs.ezra.servises.EventToJsonService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.CriteriaBuilder;
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

    @GetMapping("/")
    public String getMainPage(Model model, Principal principal) {
        UserData user = userDataRepository.findByEmail(principal.getName());
        model.addAttribute("allEvents", eventToJsonService.getAllEventsToUser(user.getId()));

        List<FamilyData> famMembers = familyDataRepository.findAllByUserOneIdOrUserTwoIdAndAreFamily(user.getId(), user.getId(), true);
        List<Integer> famMembersId = getPendingFamilyMembersId(user, famMembers);
        eventToJsonService.getAllEventsToUsersFamilyMembers(famMembersId);
        model.addAttribute("familyEvents", eventToJsonService.getAllEventsToUsersFamilyMembers(famMembersId));
        model.addAttribute("UserRoles", userRoleRepository.findAllByUserIdAndMembershipTypeIsNot(user.getId(), UserPermission.VISITOR));
        return "mainPage";
    }

    @GetMapping("/family")
    public String getFamilyPage(Model model, Principal principal){
        if (userDataRepository.findByEmail(principal.getName()) != null){
            UserData user = userDataRepository.findByEmail(principal.getName());

            model.addAttribute("user", user);
            model.addAttribute("famPendingRequest", getPendingFamilyRequests(user));
            model.addAttribute("myFamily", getMyFamily(user));
        }
        return "familyPage";
    }

    @RequestMapping( value = "/family/{memberId}", method = RequestMethod.POST)
    public String acceptOrDeleteFamilyMember(@PathVariable Integer memberId, @RequestParam boolean value,
                                            Principal principal){
        if (userDataRepository.findByEmail(principal.getName()) != null){
            UserData user = userDataRepository.findByEmail(principal.getName());
            if (userDataRepository.findById(memberId).isPresent()){
                UserData member = userDataRepository.findById(memberId).get();
                FamilyData familyData = familyDataRepository.findByFamilyId(getFamilyId(user, member));

                if (value){
                    familyData.setAreFamily(true);
                    familyData.setPendingRequest(false);
                    familyDataRepository.save(familyData);
                } else {
                    familyDataRepository.delete(familyData);
                }
            }
        }
        return "redirect:/family";
    }

    @RequestMapping(value = "/familyMember", method = RequestMethod.POST)
    public String sendFamilyMemberRequestByMail(@RequestParam String sendTo, Principal principal){
        if (sendTo.matches("^(.+)@(.+)$")){
            UserData sender = userDataRepository.findByEmail(principal.getName());
            if (userDataRepository.findByEmail(sendTo) != null){
                UserData isAlreadyUser = userDataRepository.findByEmail(sendTo);
                familyRequestRepository.save(new FamilyRequest(sender, sendTo, true));
                familyDataRepository.save(new FamilyData(sender, isAlreadyUser, true, false));
            } else {
                emailService.sendFamilyMemberRequest(sendTo, principal);
                familyRequestRepository.save(new FamilyRequest(sender, sendTo, false));
            }
        }
        return "redirect:/family";
    }

    private List<UserData> getPendingFamilyRequests(UserData user){
        List<FamilyData> pending = familyDataRepository.findAllByUserOneIdOrUserTwoIdAndPendingRequest(user.getId(), user.getId(), true);
        List<Integer> list = getPendingFamilyMembersId(user, pending);
        List<UserData> temp = new ArrayList<>();
        for (Integer i :
                list) {
            if (userDataRepository.findById(i).isPresent()){
                if (familyRequestRepository.findByUserIdAndMemberEmail(i, user.getEmail()).isPresent()){
                    temp.add(userDataRepository.findById(i).get());
                }
            }
        }
        return temp;
    }
    private List<Integer> getPendingFamilyMembersId(UserData user, List<FamilyData> pending) {
        List<Integer> pendingMemberId = new ArrayList<>();
        for (FamilyData data :
                pending) {
            String[] split = data.getFamilyId().split("_");
            int userOneId = Integer.parseInt(split[0]);
            int userTwoId = Integer.parseInt(split[1]);
            if (userOneId == user.getId()){
                pendingMemberId.add(userTwoId);
            } else {
                pendingMemberId.add(userOneId);
            }
        }
        return pendingMemberId;
    }
    private List<FamilyData> getMyFamily(UserData user){
        List<FamilyData> familyDataList = familyDataRepository.findAllByUserOneIdOrUserTwoIdAndAreFamily(user.getId(), user.getId(), true);
        List<FamilyData> familyData = new ArrayList<>();
        for (FamilyData data :
                familyDataList) {
            if (data.isAreFamily()){
                familyData.add(familyDataRepository.findByFamilyId(getFamilyId(data.getUserOne(), data.getUserTwo())));
            }
        }
        return familyData;
    }
    private String getFamilyId(UserData user, UserData member){
        String familyId;
        if (user.getId() > member.getId()){
            familyId = member.getId() + "_" + user.getId();
        } else {
            familyId = user.getId() + "_" + member.getId();
        }
        return familyId;
    }
}