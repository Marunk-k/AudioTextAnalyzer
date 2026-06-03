package com.example.audiotext.controller;

import com.example.audiotext.config.AppProperties;
import com.example.audiotext.model.Project;
import com.example.audiotext.model.ProjectStatus;
import com.example.audiotext.repository.ProjectRepository;
import com.example.audiotext.service.StorageService;
import com.example.audiotext.service.CurrentUserService;
import com.example.audiotext.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

@Controller
public class UploadController {
    private static final Logger log = LoggerFactory.getLogger(UploadController.class);
    private final StorageService storage;
    private final ProjectRepository repo;
    private final AppProperties props;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    public UploadController(StorageService storage, ProjectRepository repo, AppProperties props, CurrentUserService currentUserService, UserRepository userRepository) {
        this.storage = storage;
        this.repo = repo;
        this.props = props;
        this.currentUserService=currentUserService;
        this.userRepository=userRepository;
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
            String username = currentUserService.username();
            if (repo.existsByOwnerAndTitle(username, title.trim())) {
                model.addAttribute("error", "Проект с таким названием уже существует.");
                return uploadPage(model);
            }
            byte[] fileData = file.getBytes();
            Project p = new Project();
            p.setTitle(title.trim());
            p.setOriginalFileName(originalName);
            p.setOriginalFilePath(originalName);
            p.setStatus(ProjectStatus.UPLOADED);
            p.setUserId(userRepository.findIdByLogin(username));
            p = repo.save(p);
            repo.saveAudioFile(p.getId(), originalName, file.getContentType(), fileData);
            return "redirect:/projects/" + p.getId();
        } catch (Exception ex) {
            log.error("Upload failed for file '{}'", originalName, ex);
            model.addAttribute("error", "Не удалось сохранить файл. Проверьте имя файла и повторите попытку.");
            return uploadPage(model);
        }
    }
}
