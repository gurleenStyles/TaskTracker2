package com.userprofile.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/profile")
@CrossOrigin(origins = "*")
public class UserProfileController {
    
    // In-memory storage for demo purposes
    private Map<Long, UserProfile> profiles = new HashMap<>();
    private Long nextId = 1L;
    
    @GetMapping("/test")
    public String test() {
        return "User Profile Service is running on port 8083";
    }
    
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "User Profile Service");
        status.put("status", "UP");
        status.put("timestamp", LocalDateTime.now());
        status.put("port", 8083);
        status.put("profiles_count", profiles.size());
        return status;
    }
    
    @GetMapping
    public List<UserProfile> getAllProfiles() {
        return new ArrayList<>(profiles.values());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserProfile> getProfile(@PathVariable Long id) {
        UserProfile profile = profiles.get(id);
        return profile != null ? ResponseEntity.ok(profile) : ResponseEntity.notFound().build();
    }
    
    @PostMapping
    public UserProfile createProfile(@RequestBody CreateProfileRequest request) {
        UserProfile profile = new UserProfile(
            nextId++,
            request.firstName(),
            request.lastName(),
            request.email(),
            request.phone(),
            request.bio(),
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        profiles.put(profile.id(), profile);
        return profile;
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UserProfile> updateProfile(@PathVariable Long id, @RequestBody CreateProfileRequest request) {
        UserProfile existing = profiles.get(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        
        UserProfile updated = new UserProfile(
            id,
            request.firstName(),
            request.lastName(),
            request.email(),
            request.phone(),
            request.bio(),
            existing.createdAt(),
            LocalDateTime.now()
        );
        profiles.put(id, updated);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProfile(@PathVariable Long id) {
        UserProfile removed = profiles.remove(id);
        return removed != null ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/search")
    public List<UserProfile> searchProfiles(@RequestParam(required = false) String name,
                                           @RequestParam(required = false) String email) {
        return profiles.values().stream()
            .filter(p -> name == null || 
                (p.firstName() + " " + p.lastName()).toLowerCase().contains(name.toLowerCase()))
            .filter(p -> email == null || p.email().toLowerCase().contains(email.toLowerCase()))
            .collect(Collectors.toList());
    }
    
    // DTOs
    public static class UserProfile {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String bio;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        public UserProfile() {}
        
        public UserProfile(Long id, String firstName, String lastName, String email, 
                          String phone, String bio, LocalDateTime createdAt, LocalDateTime updatedAt) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.phone = phone;
            this.bio = bio;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }
        
        // Getters
        public Long id() { return id; }
        public String firstName() { return firstName; }
        public String lastName() { return lastName; }
        public String email() { return email; }
        public String phone() { return phone; }
        public String bio() { return bio; }
        public LocalDateTime createdAt() { return createdAt; }
        public LocalDateTime updatedAt() { return updatedAt; }
        
        // Setters
        public void setId(Long id) { this.id = id; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public void setEmail(String email) { this.email = email; }
        public void setPhone(String phone) { this.phone = phone; }
        public void setBio(String bio) { this.bio = bio; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }
    
    public static class CreateProfileRequest {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String bio;
        
        public CreateProfileRequest() {}
        
        public CreateProfileRequest(String firstName, String lastName, String email, String phone, String bio) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.phone = phone;
            this.bio = bio;
        }
        
        // Getters
        public String firstName() { return firstName; }
        public String lastName() { return lastName; }
        public String email() { return email; }
        public String phone() { return phone; }
        public String bio() { return bio; }
        
        // Setters  
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public void setEmail(String email) { this.email = email; }
        public void setPhone(String phone) { this.phone = phone; }
        public void setBio(String bio) { this.bio = bio; }
    }
}
