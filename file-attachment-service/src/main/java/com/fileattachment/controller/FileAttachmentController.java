package com.fileattachment.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.io.IOException;

@RestController
@RequestMapping("/file")
@CrossOrigin(origins = "*")
public class FileAttachmentController {
    
    // In-memory storage for demo purposes
    private Map<Long, FileAttachment> files = new HashMap<>();
    private Long nextId = 1L;
    
    @GetMapping("/test")
    public String test() {
        return "File Attachment Service is running on port 8081";
    }
    
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "File Attachment Service");
        status.put("status", "UP");
        status.put("timestamp", LocalDateTime.now());
        status.put("port", 8081);
        status.put("files_count", files.size());
        status.put("total_size_mb", files.values().stream()
            .mapToLong(f -> f.size())
            .sum() / (1024 * 1024));
        return status;
    }
    
    @GetMapping
    public List<FileAttachment> getAllFiles() {
        return new ArrayList<>(files.values());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<FileAttachment> getFile(@PathVariable Long id) {
        FileAttachment file = files.get(id);
        return file != null ? ResponseEntity.ok(file) : ResponseEntity.notFound().build();
    }
    
    @PostMapping("/upload")
    public ResponseEntity<FileAttachment> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "tags", required = false) String tags) {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            FileAttachment attachment = new FileAttachment(
                nextId++,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                description,
                tags != null ? Arrays.asList(tags.split(",")) : new ArrayList<>(),
                file.getBytes(),
                LocalDateTime.now()
            );
            files.put(attachment.id(), attachment);
            
            // Return attachment without file data for response
            FileAttachment response = new FileAttachment(
                attachment.id(),
                attachment.fileName(),
                attachment.contentType(),
                attachment.size(),
                attachment.description(),
                attachment.tags(),
                null, // Don't include file data in response
                attachment.uploadedAt()
            );
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        FileAttachment file = files.get(id);
        if (file == null || file.data() == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.fileName() + "\"")
            .contentType(MediaType.parseMediaType(file.contentType()))
            .body(file.data());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable Long id) {
        FileAttachment removed = files.remove(id);
        return removed != null ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/search")
    public List<FileAttachment> searchFiles(@RequestParam(required = false) String fileName,
                                           @RequestParam(required = false) String contentType,
                                           @RequestParam(required = false) String tag) {
        return files.values().stream()
            .filter(f -> fileName == null || f.fileName().toLowerCase().contains(fileName.toLowerCase()))
            .filter(f -> contentType == null || f.contentType().toLowerCase().contains(contentType.toLowerCase()))
            .filter(f -> tag == null || f.tags().stream().anyMatch(t -> t.toLowerCase().contains(tag.toLowerCase())))
            .map(f -> new FileAttachment(f.id(), f.fileName(), f.contentType(), f.size(), 
                f.description(), f.tags(), null, f.uploadedAt())) // Remove file data from search results
            .collect(Collectors.toList());
    }
    
    @PostMapping("/demo-files")
    public ResponseEntity<List<FileAttachment>> createDemoFiles() {
        // Create demo files for testing
        List<FileAttachment> demoFiles = Arrays.asList(
            new FileAttachment(nextId++, "project-requirements.pdf", "application/pdf", 
                2048576L, "Project requirements document", Arrays.asList("project", "requirements"), 
                "Demo PDF content".getBytes(), LocalDateTime.now()),
            new FileAttachment(nextId++, "user-guide.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", 
                1024000L, "User guide documentation", Arrays.asList("documentation", "guide"), 
                "Demo Word content".getBytes(), LocalDateTime.now()),
            new FileAttachment(nextId++, "screenshot.png", "image/png", 
                512000L, "Application screenshot", Arrays.asList("image", "screenshot"), 
                "Demo PNG content".getBytes(), LocalDateTime.now())
        );
        
        demoFiles.forEach(file -> files.put(file.id(), file));
        
        // Return without file data
        List<FileAttachment> response = demoFiles.stream()
            .map(f -> new FileAttachment(f.id(), f.fileName(), f.contentType(), f.size(), 
                f.description(), f.tags(), null, f.uploadedAt()))
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(response);
    }
    
    // DTOs
    public static class FileAttachment {
        private Long id;
        private String fileName;
        private String contentType;
        private Long size;
        private String description;
        private List<String> tags;
        private byte[] data;
        private LocalDateTime uploadedAt;
        
        public FileAttachment() {}
        
        public FileAttachment(Long id, String fileName, String contentType, Long size,
                             String description, List<String> tags, byte[] data, LocalDateTime uploadedAt) {
            this.id = id;
            this.fileName = fileName;
            this.contentType = contentType;
            this.size = size;
            this.description = description;
            this.tags = tags;
            this.data = data;
            this.uploadedAt = uploadedAt;
        }
        
        // Getters
        public Long id() { return id; }
        public String fileName() { return fileName; }
        public String contentType() { return contentType; }
        public Long size() { return size; }
        public String description() { return description; }
        public List<String> tags() { return tags; }
        public byte[] data() { return data; }
        public LocalDateTime uploadedAt() { return uploadedAt; }
        
        // Setters
        public void setId(Long id) { this.id = id; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public void setSize(Long size) { this.size = size; }
        public void setDescription(String description) { this.description = description; }
        public void setTags(List<String> tags) { this.tags = tags; }
        public void setData(byte[] data) { this.data = data; }
        public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    }
}
