package com.example.audiotext.service;

import com.example.audiotext.model.ExportFormat;
import org.springframework.beans.factory.annotation.Value;
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
    private final String uploadDir;
    private final String convertedDir;
    private final String exportDir;

    public FileSystemStorageService(@Value("${app.storage.upload-dir:data/uploads}") String uploadDir,
                                    @Value("${app.storage.converted-dir:data/converted}") String convertedDir,
                                    @Value("${app.storage.export-dir:data/exports}") String exportDir) {
        this.uploadDir = uploadDir;
        this.convertedDir = convertedDir;
        this.exportDir = exportDir;
    }

    public Path storeUploadedFile(MultipartFile file, String projectTitle) {
        try {
            String safeName = buildSafeFileName(file.getOriginalFilename());
            Path dir = Path.of(uploadDir);
            Files.createDirectories(dir);
            Path dest = dir.resolve(safeName).normalize();
            if (!dest.startsWith(dir.normalize())) throw new IllegalArgumentException("Некорректный путь сохранения файла.");
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
            try { baseName = Path.of(originalFilename).getFileName().toString(); }
            catch (InvalidPathException ignored) { baseName = originalFilename; }
        }
        String sanitized = baseName.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (sanitized.isBlank() || sanitized.equals(".") || sanitized.equals("..")) sanitized = "file";
        return UUID.randomUUID() + "_" + sanitized;
    }

    public Path createProjectDirectory(Long id) {
        try {
            Path d = Path.of(convertedDir, "project_" + id);
            Files.createDirectories(d);
            return d;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Path getConvertedPath(Long id) { return Path.of(convertedDir, "project_" + id, "audio.wav"); }

    public Path getExportPath(Long id, ExportFormat f) {
        Path d = Path.of(exportDir, "project_" + id);
        try { Files.createDirectories(d); } catch (IOException e) { throw new RuntimeException(e); }
        return d.resolve("result." + f.name().toLowerCase());
    }

    public void ensureStorageDirectoriesExist() {
        try {
            Files.createDirectories(Path.of(uploadDir));
            Files.createDirectories(Path.of(convertedDir));
            Files.createDirectories(Path.of(exportDir));
            Files.createDirectories(Path.of("data/db"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
