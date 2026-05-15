package com.example.audiotext.controller;

import com.example.audiotext.repository.ProjectRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {
    private final ProjectRepository repo;

    public HomeController(ProjectRepository repo) {
        this.repo = repo;
    }

    public record FaqItem(String question, String answer) {}

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("recentProjects", repo.findAll().stream().limit(3).toList());
        return "index";
    }

    @GetMapping("/workspace")
    public String workspace(Model model) {
        model.addAttribute("recentProjects", repo.findAll().stream().limit(5).toList());
        return "workspace";
    }

    @GetMapping("/help")
    public String help(Model model) {
        model.addAttribute("faqItems", List.of(
                new FaqItem("Какие форматы аудио поддерживаются?", "Для загрузки подходят популярные аудиоформаты, а при необходимости файл конвертируется через FFmpeg."),
                new FaqItem("Почему может использоваться mock-режим?", "Mock-режим включается, когда модель Vosk недоступна, чтобы можно было демонстрировать полный сценарий MVP."),
                new FaqItem("Куда нужно положить модель Vosk?", "Распакуйте модель в папку models/vosk-model-small-ru."),
                new FaqItem("Как работает алгоритмическая обработка?", "После распознавания текст очищается, нормализуется и подготавливается для базового анализа."),
                new FaqItem("Что делает AI-доработка?", "AI-доработка улучшает стиль и читабельность текста и сохраняет результат отдельно от исходного."),
                new FaqItem("Какие форматы экспорта доступны?", "Доступен экспорт в TXT, DOCX, PDF, JSON и SRT."),
                new FaqItem("Почему качество распознавания может быть разным?", "Точность зависит от качества записи, шума, дикции и используемой модели Vosk."),
                new FaqItem("Как подготовить аудио для лучшего результата?", "Используйте чистую запись без перегруза, с минимальным фоновым шумом и чёткой речью."),
                new FaqItem("Как подключить FFmpeg?", "Укажите путь к исполняемому файлу в параметре app.audio.ffmpeg-path."),
                new FaqItem("Где хранятся загруженные и экспортированные файлы?", "Файлы сохраняются в локальные папки data/uploads, data/converted и data/exports.")
        ));
        return "help";
    }
}
