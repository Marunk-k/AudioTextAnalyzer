package com.example.audiotext.controller;

import com.example.audiotext.repository.ProjectRepository;
import com.example.audiotext.service.CurrentUserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {
    private final ProjectRepository repo;
    private final CurrentUserService currentUserService;

    public HomeController(ProjectRepository repo, CurrentUserService currentUserService) {
        this.repo = repo;
        this.currentUserService = currentUserService;
    }

    public record HelpSection(String title, String text) {}

    @GetMapping("/")
    public String index(Model model) {
        if (currentUserService.isAuthenticated()) {
            model.addAttribute("recentProjects", repo.findRecentByOwner(currentUserService.username(), 3));
        }
        return "index";
    }

    @GetMapping("/workspace")
    public String workspace(Model model) {
        model.addAttribute("recentProjects", repo.findRecentByOwner(currentUserService.username(), 5));
        return "workspace";
    }

    @GetMapping("/help")
    public String help(Model model) {
        model.addAttribute("sections", List.of(
                new HelpSection("Регистрация и вход", "Создайте аккаунт на странице регистрации, затем войдите в систему с логином и паролем."),
                new HelpSection("Создание проекта и загрузка аудио", "Создайте проект, укажите название и загрузите аудиофайл в поддерживаемом формате."),
                new HelpSection("Обработка аудио", "После запуска обработки сервис конвертирует аудио, распознаёт речь и формирует исходный текст."),
                new HelpSection("AI-постобработка", "Вы можете запустить AI-улучшение текста, затем отредактировать и сохранить результат."),
                new HelpSection("Пользовательские словари", "Раздел словарей позволяет управлять словами-паразитами, заменами терминов и пользовательскими заменами."),
                new HelpSection("Слова-паразиты", "Добавляйте свои слова и фразы: они будут удаляться на этапе предобработки только в ваших проектах."),
                new HelpSection("Анализ текста", "Анализ считает ключевые метрики текста и использует лучшую доступную версию: manual → ai → processed → raw."),
                new HelpSection("Экспорт", "Готовый результат можно экспортировать в TXT, DOCX, PDF и JSON и повторно скачивать из истории экспорта.")
        ));
        return "help";
    }
}
