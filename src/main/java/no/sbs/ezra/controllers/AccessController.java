package no.sbs.ezra.controllers;

import lombok.AllArgsConstructor;
import no.sbs.ezra.data.*;
import no.sbs.ezra.data.repositories.*;
import no.sbs.ezra.data.validators.UserDataValidator;
import no.sbs.ezra.security.PasswordConfig;
import no.sbs.ezra.security.UserPermission;
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
public class AccessController {

    private final PasswordConfig passwordEncoder;
    private final UserDataRepository userDataRepository;
    private final BoardDataRepository boardDataRepository;
    private final UserRoleRepository userRoleRepository;
    private final FamilyRequestRepository familyRequestRepository;
    private final EventToJsonService eventToJsonService;
    private final FamilyDataRepository familyDataRepository;


    @GetMapping("/login")
    public String getLoginPage() {
        return "loginPage";
    }

    @GetMapping("/signup")
    public String getSignupPage(Model model) {
        model.addAttribute("UserData", new UserData());
        model.addAttribute("errors", new ArrayList<>());
        return "signupPage";
    }

    /*todo: implement resetPassword !*/


    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public String createNewUser(@Valid @ModelAttribute("UserData") UserData userData, BindingResult br,
                                RedirectAttributes redirectAttributes, Model model) {
        UserDataValidator validation = new UserDataValidator(userDataRepository);
        if (validation.supports(userData.getClass())) {
            validation.validate(userData, br);
        }
        if (br.hasErrors()) {
            model.addAttribute("errors", getErrorMessages(br));
            return "signupPage";
        } else {
            userData.setPassword(passwordEncoder.passwordEncoder().encode(userData.getPassword()));
            userDataRepository.save(userData);
            if (familyRequestRepository.findByMemberEmail(userData.getEmail()).isPresent()){
                FamilyRequest haveJoined = familyRequestRepository.findByMemberEmail(userData.getEmail()).get();
                haveJoined.setHaveJoined(true);
                familyRequestRepository.save(haveJoined);
                UserData sender = userDataRepository.findById(haveJoined.getUser().getId()).get();
                familyDataRepository.save(new FamilyData(sender, userData, true, false));
            }


            redirectAttributes.addFlashAttribute("username", userData.getEmail());
            return "redirect:/login";
        }
    }


    @GetMapping("/searchForBoard")
    public String getSearchForBoardPage() {
        return "searchForBoardPage";
    }

    @RequestMapping(value = "/searchForBoard", method = RequestMethod.POST)
    public String getSearchForBoardResults(Model model, @RequestParam() String keyword) {
        model.addAttribute("searchResults", getSearchResults(keyword));
        model.addAttribute("keyword", keyword);
        return "searchForBoardPage";
    }


    private List<BoardData> getSearchResults(String keyword) {
        List<BoardData> searchResults;
        if (keyword != null) {
            searchResults = boardDataRepository.search(keyword);
        } else {
            searchResults = (List<BoardData>) boardDataRepository.findAll();
        }
        if (searchResults.size() == 0) {
            searchResults.add(new BoardData("noResult", "noName", "00000000", "noEmail@no.no", "noPage"));
        }
        return searchResults;
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
