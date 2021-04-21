package no.sbs.ezra.controllers;

import no.sbs.ezra.data.BoardData;
import no.sbs.ezra.data.UserData;
import no.sbs.ezra.data.UserRole;
import no.sbs.ezra.data.repositories.BoardDataRepository;
import no.sbs.ezra.data.repositories.UserRoleRepository;
import no.sbs.ezra.data.validators.BoardDataValidator;
import no.sbs.ezra.data.validators.UserDataValidator;
import no.sbs.ezra.data.repositories.UserDataRepository;
import no.sbs.ezra.security.PasswordConfig;
import no.sbs.ezra.security.UserPermission;
import no.sbs.ezra.servises.PermissionService;
import no.sbs.ezra.servises.EventToJsonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Optional;


@Controller
public class AccessController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PasswordConfig passwordEncoder;
    private final UserDataRepository userDataRepository;
    private final BoardDataRepository boardDataRepository;
    private final UserRoleRepository userRoleRepository;
    private final EventToJsonService eventToJsonService;
    private final PermissionService permissionService;


    public AccessController(PasswordConfig passwordEncoder, UserDataRepository userDataRepository, BoardDataRepository boardDataRepository, UserRoleRepository userRoleRepository, EventToJsonService eventToJsonService, PermissionService permissionService) {
        this.passwordEncoder = passwordEncoder;
        this.userDataRepository = userDataRepository;
        this.boardDataRepository = boardDataRepository;
        this.userRoleRepository = userRoleRepository;
        this.eventToJsonService = eventToJsonService;
        this.permissionService = permissionService;
    }

    @GetMapping("/")
    public String homePage(Model model, Principal principal){
        UserData user = userDataRepository.findByEmail(principal.getName());
        List<UserRole> listOfUserRoles = userRoleRepository.findAllByUserId(user.getId());
        List<BoardData> boards = new ArrayList<>();
        List<UserRole> urBoards = new ArrayList<>();
        for (UserRole ur :
                listOfUserRoles) {
            Optional<BoardData> temp = boardDataRepository.findById(ur.getBoard().getId());
            boards.add(temp.orElseThrow());
            boards.removeIf(bd -> ur.getMembershipType().getPermission().equals("visitor"));
        }

        model.addAttribute("allEvents", eventToJsonService.getAllEventsToUser(user.getId()));
        model.addAttribute("myBoards", boards);
        return "mainPage";
    }


    @GetMapping("/login")
    public String getLoginPage(){
        return "loginPage";
    }

    @GetMapping("/signup")
    public String getSignupPage(Model model){
        model.addAttribute("UserData", new UserData());
        model.addAttribute("errors", new ArrayList());
        return "signupPage";
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public String createNewUser(@Valid @ModelAttribute("UserData") UserData userData, BindingResult br,
                                RedirectAttributes redirectAttributes, Model model){
        UserDataValidator validation = new UserDataValidator(userDataRepository);
        if (validation.supports(userData.getClass())) {
            validation.validate(userData, br);
        } else {
            logger.error("Failed to support UserData class and or validate UserData");
        }
        if (br.hasErrors()) {
            model.addAttribute("errors", getErrorMessages(br));
            return "signupPage";
        } else {
            userData.setPassword(passwordEncoder.passwordEncoder().encode(userData.getPassword()));
            userDataRepository.save(userData);
            logger.info(userData.getEmail()+" successfully signed up");
            redirectAttributes.addFlashAttribute("username", userData.getEmail());
            return "redirect:/login";
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

    @GetMapping("/searchForBoard")
    public String getBoardsPage(){
        return "searchForBoardPage";
    }
    @RequestMapping(value = "/searchForBoard", method = RequestMethod.POST)
    public String searchForBoards(Model model, @RequestParam() String keyword) {
        List<BoardData> searchResults;
        if (keyword != null) {
            searchResults = boardDataRepository.search(keyword);
        } else {
            searchResults = (List<BoardData>) boardDataRepository.findAll();
        }
        if (searchResults.size() == 0){
            searchResults.add( new BoardData("noResult", "noName", "00000000", "noEmail@no.no", "noPage"));
        }
        model.addAttribute("searchResults", searchResults);
        model.addAttribute("keyword", keyword);
        return "searchForBoardPage";
    }

    @GetMapping("/newBoard")
    public String createNewBoard(Model model, Principal principal){
        model.addAttribute("user", userDataRepository.findByEmail(principal.getName()));
        model.addAttribute("BoardData", new BoardData());
        return "newBoardPage";
    }

    @RequestMapping(value = "/createBoard", method = RequestMethod.POST)
    public String createNewBoard(@Valid @ModelAttribute("BoardData") BoardData boardData, BindingResult br,
                                 RedirectAttributes redirectAttributes, Principal principal, Model model){
        BoardDataValidator validator = new BoardDataValidator();
        UserData user = userDataRepository.findByEmail(principal.getName());
        model.addAttribute("user", user);
        if (validator.supports(boardData.getClass())) {
            validator.validate(boardData, br);
        } else {
            logger.error("Failed to support BoardData class and or validate BoardData");
        }
        if (br.hasErrors()) {
            return "newBoardPage";
        } else {
            boardDataRepository.save(boardData);
            logger.info(boardData.getName() + " was successfully created by: " + userDataRepository.findByEmail(principal.getName()));
            userRoleRepository.save(new UserRole(user, boardData, UserPermission.MASTER, false));
            logger.info("UserRole appended successfully");
        }
        return "redirect:/";
    }

}
