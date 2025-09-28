package com.tasktracker.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(@AuthenticationPrincipal User user, Model model) {
        if (user != null) {
            model.addAttribute("username", user.getUsername());//loged in user info
            model.addAttribute("message", "Welcome to TaskTracker! You are successfully logged in.");
        }
        return "home";//looks for home.html file in templates folder
    }
    
    @GetMapping("/microservices-test")
    public String microservicesTest(@AuthenticationPrincipal User user, Model model) {
        if (user != null) {
            model.addAttribute("username", user.getUsername());
        }
        return "microservices-test";
    }
}
