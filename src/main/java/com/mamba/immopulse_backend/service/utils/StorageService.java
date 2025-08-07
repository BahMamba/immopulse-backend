package com.mamba.immopulse_backend.service.utils;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class StorageService {

    private final Path uploadDir = Paths.get("uploads");

    public StorageService() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la création du dossier uploads", e);
        }
    }

    // Sauvegarde un nouveau fichier
    public String save(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                throw new RuntimeException("Nom de fichier invalide ou manquant");
            }

            String ext = originalFilename.substring(originalFilename.lastIndexOf('.'));
            String uniqName = UUID.randomUUID().toString() + ext;

            Path filePath = uploadDir.resolve(uniqName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + uniqName;

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'enregistrement du fichier", e);
        }
    }

    // Supprime une ancienne image (optionnel)
    public void delete(String relativePath) {
        try {
            if (relativePath != null && !relativePath.isBlank()) {
                Path fullPath = uploadDir.resolve(Paths.get(relativePath.replace("/uploads/", "")));
                Files.deleteIfExists(fullPath);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la suppression de l'image précédente : " + e.getMessage());
        }
    }
}
