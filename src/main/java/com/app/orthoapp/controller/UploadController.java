package com.app.orthoapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
@RequestMapping("/admin")
public class UploadController {

    @PostMapping("/admin/upload-uploads-zip")
    public ResponseEntity<String> uploadUploadsZip(@RequestParam("file") MultipartFile zipFile) {
        try {
            // Dossier de destination : /uploads
            Path uploadsDir = Paths.get("uploads");

            // 🔁 S'assurer que "uploads" est un dossier
            File uploadsFile = uploadsDir.toFile();
            if (uploadsFile.exists() && !uploadsFile.isDirectory()) {
                uploadsFile.delete(); // ❌ supprime si c’est un fichier
            }
            Files.createDirectories(uploadsDir);

            // 🔧 Sauvegarder temporairement le zip
            Path tempZip = Files.createTempFile("uploads-", ".zip");
            zipFile.transferTo(tempZip.toFile());

            // ✅ Extraction
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(tempZip.toFile()))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    Path filePath = uploadsDir.resolve(entry.getName());
                    if (entry.isDirectory()) {
                        Files.createDirectories(filePath);
                    } else {
                        Files.createDirectories(filePath.getParent()); // dossier parent
                        Files.copy(zis, filePath, StandardCopyOption.REPLACE_EXISTING);
                    }
                    zis.closeEntry();
                }
            }

            return ResponseEntity.ok("✅ Fichiers uploads restaurés avec succès !");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Erreur : " + e.getMessage());
        }
    }
}
