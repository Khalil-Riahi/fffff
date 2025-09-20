// src/main/java/com/projet/freelencetinder/storage/FileStorageService.java
package com.projet.freelencetinder.servcie;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    @Value("${app.upload-dir:uploads}")   // configurable
    private String uploadDir;

    /** Enregistre un fichier et retourne son URL publique « /uploads/xxxxx.ext » */
    public String save(MultipartFile file) throws IOException {

        if (file.isEmpty())
            throw new IllegalArgumentException("Fichier vide");

        String original = StringUtils.cleanPath(file.getOriginalFilename());
        String ext      = StringUtils.getFilenameExtension(original);
        String name     = UUID.randomUUID() + (ext != null ? "." + ext : "");

        Path dir  = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);                    // crée dossier si besoin

        Path target = dir.resolve(name);
        file.transferTo(target.toFile());

        return "/uploads/" + name;                       // URL accessible
    }
}
