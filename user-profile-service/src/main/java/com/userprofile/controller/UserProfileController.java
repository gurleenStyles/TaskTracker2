package com.userprofile.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    
    @GetMapping(produces = "application/json")
    public List<UserProfile> getAllProfiles() {
        return new ArrayList<>(profiles.values());
    }
    
    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<UserProfile> getProfile(@PathVariable Long id) {
        UserProfile profile = profiles.get(id);
        return profile != null ? ResponseEntity.ok(profile) : ResponseEntity.notFound().build();
    }
    
    @PostMapping(consumes = "application/json", produces = "application/json")
    public UserProfile createProfile(@RequestBody CreateProfileRequest request) {
        UserProfile profile = new UserProfile(
            nextId++,
            request.getFirstName(),
            request.getLastName(),
            request.getEmail(),
            request.getPhone(),
            request.getBio(),
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        profiles.put(profile.getId(), profile);
        return profile;
    }
    
    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserProfile> updateProfile(@PathVariable Long id, @RequestBody CreateProfileRequest request) {
        UserProfile existing = profiles.get(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        
        UserProfile updated = new UserProfile(
            id,
            request.getFirstName(),
            request.getLastName(),
            request.getEmail(),
            request.getPhone(),
            request.getBio(),
            existing.getCreatedAt(),
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
    
    @GetMapping(value = "/search", produces = "application/json")
    public List<UserProfile> searchProfiles(@RequestParam(required = false) String name,
                                           @RequestParam(required = false) String email) {
        return profiles.values().stream()
            .filter(p -> name == null || 
                (p.getFirstName() + " " + p.getLastName()).toLowerCase().contains(name.toLowerCase()))
            .filter(p -> email == null || p.getEmail().toLowerCase().contains(email.toLowerCase()))
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
        
        // JavaBean-style Getters (required by Jackson)
        public Long getId() { return id; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getBio() { return bio; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        
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
        
        // JavaBean-style Getters
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getBio() { return bio; }
        
        // Setters  
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public void setEmail(String email) { this.email = email; }
        public void setPhone(String phone) { this.phone = phone; }
        public void setBio(String bio) { this.bio = bio; }
    }
}
