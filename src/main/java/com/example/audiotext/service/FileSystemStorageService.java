package com.example.audiotext.service;

import com.example.audiotext.config.AppProperties;
import com.example.audiotext.model.ExportFormat;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileSystemStorageService implements StorageService {
    private final AppProperties p;

    public FileSystemStorageService(AppProperties p) {
        this.p = p;
    }

    public Path storeUploadedFile(MultipartFile file, String projectTitle) {
        try {
            String safeName = buildSafeFileName(file.getOriginalFilename());
            Path uploadDir = Path.of(p.getStorage().getUploadDir());
            Files.createDirectories(uploadDir);
            Path dest = uploadDir.resolve(safeName).normalize();
            if (!dest.startsWith(uploadDir.normalize())) {
                throw new IllegalArgumentException("Некорректный путь сохранения файла.");
            }
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            }
            return dest;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String buildSafeFileName(String originalFilename) {
        String baseName = "file";
        if (originalFilename != null && !originalFilename.isBlank()) {
            try {
                baseName = Path.of(originalFilename).getFileName().toString();
            } catch (InvalidPathException ignored) {
                baseName = originalFilename;
            }
        }
        String sanitized = baseName.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (sanitized.isBlank() || sanitized.equals(".") || sanitized.equals("..")) sanitized = "file";
        return UUID.randomUUID() + "_" + sanitized;
    }

    public Path createProjectDirectory(Long id) {
        try {
            Path d = Path.of(p.getStorage().getConvertedDir(), "project_" + id);
            Files.createDirectories(d);
            return d;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Path getConvertedPath(Long id) {
        return Path.of(p.getStorage().getConvertedDir(), "project_" + id, "audio.wav");
    }

    public Path getExportPath(Long id, ExportFormat f) {
        Path d = Path.of(p.getStorage().getExportDir(), "project_" + id);
        try {
            Files.createDirectories(d);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return d.resolve("result." + f.name().toLowerCase());
    }

    public void ensureStorageDirectoriesExist() {
        try {
            Files.createDirectories(Path.of(p.getStorage().getUploadDir()));
            Files.createDirectories(Path.of(p.getStorage().getConvertedDir()));
            Files.createDirectories(Path.of(p.getStorage().getExportDir()));
            Files.createDirectories(Path.of("data/db"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
