package com.calio.tracking.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/meals/upload-image")
public class TrackingImageController {

    private final String UPLOAD_DIR = "/app/images/";

    @PostMapping
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El archivo está vacío"));
        }

        try {
            // Asegurar que el directorio exista
            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Generar un nombre de archivo único
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(UPLOAD_DIR, newFilename);

            // Guardar el archivo
            Files.write(filePath, file.getBytes());

            // Devolver la URL relativa (que será servida por WebConfig a través de /images/**)
            // IMPORTANTE: el API Gateway enruta /api/tracking/** -> Tracking Service
            String fileUrl = "/api/tracking/images/" + newFilename;
            return ResponseEntity.ok(Map.of("imageUrl", fileUrl));

        } catch (IOException e) {
            log.error("Error al guardar la imagen", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al procesar la imagen"));
        }
    }
}
