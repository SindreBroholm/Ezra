package no.sbs.ezra.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccessController {

    @GetMapping("/login")
    public String getLoginPage(){

        return "loginPage";
    }

    @GetMapping("/")
    public String homePage(){

        return "mainPage";
    }
}
