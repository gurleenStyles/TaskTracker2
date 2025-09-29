package com.fileattachment.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;

@RestController
@RequestMapping("/file")
@CrossOrigin(origins = "*")
public class FileAttachmentController {
    
    // In-memory storage for demo purposes
    private final Map<Long, FileAttachment> files = new HashMap<>();
    private Long nextId = 1L;
    
    @GetMapping("/test")
    public String test() {
        return "File Attachment Service is running on port 8081";
    }
    
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "File Attachment Service");
        status.put("status", "UP");
        status.put("timestamp", LocalDateTime.now());
        status.put("port", 8081);
        status.put("files_count", files.size());
        long totalBytes = files.values().stream()
            .mapToLong(f -> f.getSize() != null ? f.getSize() : 0L)
            .sum();
        status.put("total_size_mb", totalBytes / (1024.0 * 1024.0));
        return status;
    }
    
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FileAttachment> getAllFiles() {
        // data field is @JsonIgnore so it won't be included
        return new ArrayList<>(files.values());
    }
    
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FileAttachment> getFile(@PathVariable Long id) {
        FileAttachment file = files.get(id);
        return file != null ? ResponseEntity.ok(file) : ResponseEntity.notFound().build();
    }
    
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FileAttachment> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "tags", required = false) String tags) {
        
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            FileAttachment attachment = new FileAttachment(
                nextId++,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                description,
                tags != null ? Arrays.stream(tags.split(",")).map(String::trim).collect(Collectors.toList()) : new ArrayList<>(),
                file.getBytes(),
                LocalDateTime.now()
            );
            files.put(attachment.getId(), attachment);
            
            // Return stored attachment (data is ignored by JSON because of @JsonIgnore)
            return ResponseEntity.ok(attachment);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping(value = "/{id}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        FileAttachment file = files.get(id);
        if (file == null || file.getData() == null) {
            return ResponseEntity.notFound().build();
        }
        
        String contentType = file.getContentType();
        MediaType mediaType;
        try {
            mediaType = (contentType != null && !contentType.isBlank())
                    ? MediaType.parseMediaType(contentType)
                    : MediaType.APPLICATION_OCTET_STREAM;
        } catch (Exception ex) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
            .contentType(mediaType)
            .contentLength(file.getSize() != null ? file.getSize() : file.getData().length)
            .body(file.getData());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable Long id) {
        FileAttachment removed = files.remove(id);
        return removed != null ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
    
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FileAttachment> searchFiles(@RequestParam(required = false) String fileName,
                                           @RequestParam(required = false) String contentType,
                                           @RequestParam(required = false) String tag) {
        return files.values().stream()
            .filter(f -> fileName == null || (f.getFileName() != null && f.getFileName().toLowerCase().contains(fileName.toLowerCase())))
            .filter(f -> contentType == null || (f.getContentType() != null && f.getContentType().toLowerCase().contains(contentType.toLowerCase())))
            .filter(f -> tag == null || (f.getTags() != null && f.getTags().stream().anyMatch(t -> t.toLowerCase().contains(tag.toLowerCase()))))
            .collect(Collectors.toList());
    }
    
    @PostMapping(value = "/demo-files", produces = MediaType.APPLICATION_JSON_VALUE)
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
        
        demoFiles.forEach(file -> files.put(file.getId(), file));
        return ResponseEntity.ok(demoFiles);
    }
    
    // DTOs
    public static class FileAttachment {
        private Long id;
        private String fileName;
        private String contentType;
        private Long size;
        private String description;
        private List<String> tags;
        @JsonIgnore
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
        
        // JavaBean-style Getters required by Jackson
        public Long getId() { return id; }
        public String getFileName() { return fileName; }
        public String getContentType() { return contentType; }
        public Long getSize() { return size; }
        public String getDescription() { return description; }
        public List<String> getTags() { return tags; }
        public byte[] getData() { return data; }
        public LocalDateTime getUploadedAt() { return uploadedAt; }
        
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
