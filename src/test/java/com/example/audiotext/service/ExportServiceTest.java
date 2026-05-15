package com.example.audiotext.service;

import com.example.audiotext.model.ExportFormat;
import com.example.audiotext.model.Project;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportServiceTest {
    @Test
    void exportUsesAiTextAsMainTextWhenAvailable() throws Exception {
        Path out = Files.createTempFile("export-test", ".txt");
        StorageService storage = new StorageService() {
            public Path storeUploadedFile(MultipartFile file, String projectTitle) { return null; }
            public Path createProjectDirectory(Long projectId) { return null; }
            public Path getConvertedPath(Long projectId) { return null; }
            public Path getExportPath(Long projectId, ExportFormat format) { return out; }
            public void ensureStorageDirectoriesExist() {}
        };

        ExportService service = new ExportService(storage);
        Project p = new Project();
        p.setId(1L);
        p.setTitle("t");
        p.setOriginalFileName("f");
        p.setRawText("raw");
        p.setProcessedText("processed");
        p.setAiText("AI финальный текст");

        service.exportToTxt(p);

        String exported = Files.readString(out);
        assertTrue(exported.contains("Главный текст:\nAI финальный текст"));
    }
}
