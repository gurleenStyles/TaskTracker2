package com.tasktracker.web;//defines where this class belongs

import org.springframework.stereotype.Controller;//Marks the class as a Spring MVC controller that handles web requests.
import org.springframework.web.bind.annotation.GetMapping;//Maps HTTP GET requests to specific methods.

@Controller//this is a spring controller.This tells Spring Boot: “This is a web controller. Scan it and use it to handle requests.” 
public class LoginController {

    @GetMapping("/login")//annotation tells Spring: “Whenever a user visits the /login URL with a GET request, run this method.”
    public String loginPage() {
        return "login";//this doesn't returns any text instead it looks for a view html file named as login
    }
}
