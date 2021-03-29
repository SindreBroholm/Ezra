package no.sbs.ezra.controllers;

import no.sbs.ezra.data.BoardData;
import no.sbs.ezra.data.UserData;
import no.sbs.ezra.data.repositories.BoardDataRepository;
import no.sbs.ezra.data.validators.UserDataValidator;
import no.sbs.ezra.data.repositories.UserDataRepository;
import no.sbs.ezra.security.PasswordConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;


@Controller
public class AccessController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PasswordConfig passwordEncoder;
    private final UserDataRepository userDataRepository;
    private final BoardDataRepository boardDataRepository;


    public AccessController(PasswordConfig passwordEncoder, UserDataRepository userDataRepository, BoardDataRepository boardDataRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userDataRepository = userDataRepository;
        this.boardDataRepository = boardDataRepository;
    }

    @GetMapping("/")
    public String homePage(){

        return "mainPage";
    }

    @GetMapping("/login")
    public String getLoginPage(){
        return "loginPage";
    }

    @GetMapping("/signup")
    public String getSignupPage(Model model){
        model.addAttribute("UserData", new UserData());
        return "signupPage";
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public String createNewUser(@Valid @ModelAttribute("UserData") UserData userData, BindingResult br,
                                RedirectAttributes redirectAttributes){
        UserDataValidator validation = new UserDataValidator(userDataRepository);
        if (validation.supports(userData.getClass())) {
            validation.validate(userData, br);
        } else {
            logger.error("Failed to support UserData class and or validate UserData");
        }
        if (br.hasErrors()) {
            return "signupPage";
        } else {
            userData.setPassword(passwordEncoder.passwordEncoder().encode(userData.getPassword()));
            userDataRepository.save(userData);
            logger.info(userData.getEmail()+" successfully signed up");
            redirectAttributes.addFlashAttribute("username", userData.getEmail());
            return "redirect:/login";
        }
    }

    @GetMapping("/boards")
    public String getBoardsPage(){



        return "boardsPage";
    }
    @RequestMapping(value = "/searchForBoard", method = RequestMethod.POST)
    public String searchForBoards(Model model, @RequestParam() String keyword) {
        List<BoardData> searchResults;
        if (keyword != null) {
            searchResults = boardDataRepository.search(keyword);
        } else {
            searchResults = (List<BoardData>) boardDataRepository.findAll();
        }
        model.addAttribute("searchResults", searchResults);
        model.addAttribute("keyword", keyword);
        return "boardsPage";
    }

}
