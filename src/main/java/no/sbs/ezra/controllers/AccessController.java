package no.sbs.ezra.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RestController("/")
public class AccessController {

    @GetMapping("login")
    public String getLoginPage(){

        return "loginPage";
    }
}
