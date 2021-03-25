package no.sbs.ezra.controllers;

import no.sbs.ezra.data.UserData;
import no.sbs.ezra.data.validators.UserDataValidator;
import no.sbs.ezra.repositories.UserDataRepository;
import no.sbs.ezra.security.PasswordConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;


@Controller
public class AccessController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PasswordConfig passwordEncoder;
    private final UserDataRepository userDataRepository;


    public AccessController(PasswordConfig passwordEncoder, UserDataRepository userDataRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userDataRepository = userDataRepository;
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
    public String createNewUser(@Valid @ModelAttribute("UserData") UserData userData, BindingResult br, RedirectAttributes redirectAttributes, Model model){
        UserDataValidator validation = new UserDataValidator(userDataRepository);
        logger.info(userData.toString());
        if (validation.supports(userData.getClass())) {
            logger.info("starting validation");
            logger.info(userData.toString());
            validation.validate(userData, br);
        } else {
            logger.error("Failed to support UserData class and or validate UserData");
        }
        if (br.hasErrors()) {
            logger.error("Validation failed, errorCount:" + br.getErrorCount() +"\n"+ br.getAllErrors());
            return "signupPage";
        } else {
            userData.setPassword(passwordEncoder.passwordEncoder().encode(userData.getPassword()));
            userDataRepository.save(userData);
            logger.info("User successfully signed up");
            redirectAttributes.addFlashAttribute("user", userData);
            return "redirect:/login";
        }
    }
}
