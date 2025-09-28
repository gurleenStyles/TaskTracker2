package com.userprofile.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserProfileController {
    @GetMapping("/profile/test")
    public String test() {
        return "User Profile Service is running";
    }
}
