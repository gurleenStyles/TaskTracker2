package com.tasktracker.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tasktracker.model.AppUser;
import com.tasktracker.model.Role;
import com.tasktracker.service.UserService;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // This runs first after the javascript is called step 1
    @PostMapping("/register")
    public MeResponse register(@RequestBody RegisterRequest req) {
        AppUser user = userService.registerUser(req.username(), req.password(), req.email());
        return new MeResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        AppUser appUser = userService.getUserByUsername(user.getUsername());
        return new MeResponse(appUser.getId(), appUser.getUsername(), appUser.getEmail(), appUser.getRole());
    }

    // Request/Response DTOs
    public static record RegisterRequest(@NotBlank String username,
            @NotBlank String password, // can't be empty
            @Email String email) { // must be a valid email or null
    }

    public static record MeResponse(Long id, String username, String email, Role role) {
    }
}
