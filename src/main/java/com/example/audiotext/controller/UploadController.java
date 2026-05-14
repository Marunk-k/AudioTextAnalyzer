package com.example.audiotext.controller;

import com.example.audiotext.config.AppProperties;
import com.example.audiotext.model.Project;
import com.example.audiotext.model.ProjectStatus;
import com.example.audiotext.repository.ProjectRepository;
import com.example.audiotext.service.StorageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

@Controller
public class UploadController {
    private final StorageService storage;
    private final ProjectRepository repo;
    private final AppProperties props;

    public UploadController(StorageService storage, ProjectRepository repo, AppProperties props) {
        this.storage = storage;
        this.repo = repo;
        this.props = props;
    }

    @GetMapping("/upload")
    public String uploadPage(Model model) {
        model.addAttribute("allowedFormats", String.join(", ", props.getAudio().getAllowedExtensions()).toUpperCase(Locale.ROOT));
        return "upload";
    }

    @PostMapping("/upload")
    public String doUpload(@RequestParam String title, @RequestParam MultipartFile file, Model model) {
        if (title == null || title.isBlank()) {
            model.addAttribute("error", "Введите название проекта.");
            return uploadPage(model);
        }
        if (file == null || file.isEmpty()) {
            model.addAttribute("error", "Файл не выбран.");
            return uploadPage(model);
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            model.addAttribute("error", "Не удалось определить имя загруженного файла.");
            return uploadPage(model);
        }

        String lowerName = originalName.toLowerCase(Locale.ROOT);
        boolean supported = props.getAudio().getAllowedExtensions().stream()
                .map(ext -> "." + ext.toLowerCase(Locale.ROOT))
                .anyMatch(lowerName::endsWith);
        if (!supported) {
            model.addAttribute("error", "Формат файла не поддерживается. Разрешены: WAV, MP3, M4A, OGG, FLAC.");
            return uploadPage(model);
        }

        try {
            var path = storage.storeUploadedFile(file, title);
            Project p = new Project();
            p.setTitle(title.trim());
            p.setOriginalFileName(originalName);
            p.setOriginalFilePath(path.toString());
            p.setStatus(ProjectStatus.UPLOADED);
            p = repo.save(p);
            return "redirect:/projects/" + p.getId();
        } catch (Exception ex) {
            model.addAttribute("error", "Не удалось сохранить файл. Проверьте имя файла и повторите попытку.");
            return uploadPage(model);
        }
    }
}
