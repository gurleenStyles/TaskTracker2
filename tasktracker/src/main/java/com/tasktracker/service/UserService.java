package com.tasktracker.service;

import com.tasktracker.model.AppUser;
import com.tasktracker.model.Role;
import com.tasktracker.repo.AppUserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    public UserService(AppUserRepository appUserRepository,
            BCryptPasswordEncoder passwordEncoder,
            NotificationService notificationService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    /**
     * Register a new user
     */
    public AppUser registerUser(String username, String password, String email) {
        // Validate inputs
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        // Check if username already exists
        if (appUserRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email already exists (if provided)
        if (email != null && !email.trim().isEmpty() && appUserRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create new user
        AppUser user = new AppUser(username.trim(), passwordEncoder.encode(password), email);
        user.setRole(Role.USER);
        AppUser savedUser = appUserRepository.save(user);

        // Send welcome email
        try {
            notificationService.sendWelcomeEmail(savedUser);
        } catch (Exception e) {
            System.out.println(
                    "[REGISTRATION] Welcome email failed for " + savedUser.getUsername() + ": " + e.getMessage());
        }

        return savedUser;
    }

    /**
     * Find user by username
     */
    public Optional<AppUser> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        return appUserRepository.findByUsername(username);
    }

    /**
     * Find user by email
     */
    public Optional<AppUser> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        return appUserRepository.findByEmail(email);
    }

    /**
     * Find user by ID
     */
    public Optional<AppUser> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return appUserRepository.findById(id);
    }

    /**
     * Check if username exists
     */
    public boolean usernameExists(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return appUserRepository.existsByUsername(username);
    }

    /**
     * Check if email exists
     */
    public boolean emailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return appUserRepository.existsByEmail(email);
    }

    /**
     * Update user profile
     */
    public AppUser updateUserProfile(Long userId, String email) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if email is being changed and if new email already exists
        if (email != null && !email.trim().isEmpty() &&
                !email.equals(user.getEmail()) && appUserRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (email != null) {
            user.setEmail(email.trim().isEmpty() ? null : email.trim());
        }

        return appUserRepository.save(user);
    }

    /**
     * Change user password
     */
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Validate new password
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password is required");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        appUserRepository.save(user);
    }

    /**
     * Get user by username, throw exception if not found
     */
    public AppUser getUserByUsername(String username) {
        return findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    /**
     * Get user by ID, throw exception if not found
     */
    public AppUser getUserById(Long id) {
        return findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
}