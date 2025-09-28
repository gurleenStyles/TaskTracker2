package com.fileattachment.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FileAttachmentController {
    @GetMapping("/file/test")
    public String test() {
        return "File Attachment Service is running";
    }
}
