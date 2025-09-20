package com.projet.freelencetinder.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.projet.freelencetinder.servcie.FileStorageService;

@RestController
@RequestMapping("/api/files")
public class UploadController {

    private final FileStorageService storage;

    // ✅ Constructeur explicite pour initialiser `storage`
    public UploadController(FileStorageService storage) {
        this.storage = storage;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String url = storage.save(file);
        return ResponseEntity.ok(url); // renvoie "/uploads/xxxx.jpg"
    }
}
