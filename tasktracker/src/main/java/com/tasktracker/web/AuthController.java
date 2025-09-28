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

import com.tasktracker.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserDetailsService userDetailsService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // This runs first after the javascript is called step 1
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            AppUser user = userService.registerUser(req.username(), req.password(), req.email());
            MeResponse response = new MeResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Validation Error");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Server Error");
            error.put("message", "Registration failed. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
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

    // JWT login endpoint
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password()));
            if (authentication.isAuthenticated()) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(req.username());
                String token = jwtUtil.generateToken(userDetails.getUsername());
                Map<String, String> response = new HashMap<>();
                response.put("token", token);
                response.put("username", userDetails.getUsername());
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication Failed");
                error.put("message", "Invalid credentials");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication Failed");
            error.put("message", "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    public static record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }
}
