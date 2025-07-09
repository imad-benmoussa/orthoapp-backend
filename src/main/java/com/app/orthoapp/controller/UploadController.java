package com.app.orthoapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
@RequestMapping("/admin")
public class UploadController {

    @PostMapping("/upload-uploads-zip")
    public ResponseEntity<String> uploadZip(@RequestParam("file") MultipartFile file) {
        try {
            // 📁 Créer le dossier destination s’il n’existe pas
            File uploadDir = new File("/home/render/orthoapp/uploads");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 💾 Enregistrer temporairement le fichier zip
            File tempZip = new File("/home/render/orthoapp/uploads.zip");
            file.transferTo(tempZip);

            // 📦 Extraire le contenu du zip
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(tempZip))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    File outFile = new File(uploadDir, entry.getName());

                    // Créer les sous-dossiers si nécessaire
                    outFile.getParentFile().mkdirs();

                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        zis.transferTo(fos);
                    }
                    zis.closeEntry();
                }
            }

            return ResponseEntity.ok("✅ Fichiers extraits avec succès.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("❌ Erreur : " + e.getMessage());
        }
    }
}
