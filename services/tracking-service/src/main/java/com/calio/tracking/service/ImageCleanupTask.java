package com.calio.tracking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
public class ImageCleanupTask {

    private final String UPLOAD_DIR = "/app/images/";
    private final int MAX_AGE_HOURS = 48;

    // Ejecutar cada hora en el minuto 0
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupOldImages() {
        log.info("Iniciando tarea de limpieza de imágenes...");
        File directory = new File(UPLOAD_DIR);

        if (!directory.exists() || !directory.isDirectory()) {
            log.info("Directorio de imágenes no existe o está vacío. Abortando limpieza.");
            return;
        }

        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            log.info("No hay imágenes para limpiar.");
            return;
        }

        Instant cutoffTime = Instant.now().minus(MAX_AGE_HOURS, ChronoUnit.HOURS);
        int deletedCount = 0;

        for (File file : files) {
            try {
                if (file.isFile()) {
                    BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                    Instant creationTime = attr.creationTime().toInstant(); // o lastModifiedTime() si se prefiere

                    // Si la imagen es más antigua que el límite (48h), se borra
                    if (creationTime.isBefore(cutoffTime)) {
                        boolean deleted = file.delete();
                        if (deleted) {
                            deletedCount++;
                            log.debug("Imagen eliminada por antigüedad (> 48h): {}", file.getName());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error al procesar el archivo para limpieza: {}", file.getName(), e);
            }
        }

        log.info("Tarea de limpieza finalizada. Imágenes eliminadas: {}", deletedCount);
    }
}
