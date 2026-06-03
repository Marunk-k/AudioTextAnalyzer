package com.example.audiotext.controller;

import com.example.audiotext.model.TranscriptionResult;
import com.example.audiotext.repository.ProjectRepository;
import com.example.audiotext.service.CurrentUserService;
import com.example.audiotext.service.GigaChatService;
import com.example.audiotext.service.TextAnalysisService;
import com.example.audiotext.service.TextVersionSelector;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AnalysisController {
    private final ProjectRepository repo;
    private final GigaChatService ai;
    private final TextAnalysisService analysis;
    private final CurrentUserService currentUserService;

    public AnalysisController(ProjectRepository repo, GigaChatService ai, TextAnalysisService analysis,
                              CurrentUserService currentUserService) {
        this.repo = repo;
        this.ai = ai;
        this.analysis = analysis;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/projects/{id}/text/ai-improve")
    public String aiImprove(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String username = currentUserService.username();
        var project = repo.findByIdAndOwner(id, username).orElseThrow();
        String baseText = TextVersionSelector.hasText(project.getProcessedText())
                ? project.getProcessedText()
                : project.getRawText();

        if (!TextVersionSelector.hasText(baseText)) {
            redirectAttributes.addFlashAttribute("warning", "Нет текста для AI-постобработки.");
            return "redirect:/projects/" + id;
        }

        try {
            String aiText = ai.improveText(baseText);
            if (!TextVersionSelector.hasText(aiText)) {
                redirectAttributes.addFlashAttribute("warning", "GigaChat вернул пустой результат. Текущий текст проекта не изменён.");
                return "redirect:/projects/" + id;
            }

            project.setAiText(aiText.trim());
            var analysisContext = new TranscriptionResult();
            analysisContext.setDurationSeconds(project.getDurationSeconds() != null ? project.getDurationSeconds() : 0);
            var selected = TextVersionSelector.selectedText(project);
            var analysisResult = analysis.analyze(selected.text(), analysisContext, username, project.getRawText());
            analysisResult.sourceTextType = selected.type().name();
            project.setAnalysisResult(analysisResult);
            repo.update(project);
            repo.updateAnalysis(id, analysisResult);
            redirectAttributes.addFlashAttribute("success", "AI-постобработка выполнена.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute(
                    "warning",
                    "AI-постобработка временно недоступна или не настроена. Можно продолжить работу с текущим текстом."
            );
        }
        return "redirect:/projects/" + id;
    }
}
