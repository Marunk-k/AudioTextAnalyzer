package com.example.audiotext.controller;

import com.example.audiotext.model.Project;
import com.example.audiotext.model.TextAnalysisResult;
import com.example.audiotext.repository.ProjectRepository;
import com.example.audiotext.service.GigaChatService;
import com.example.audiotext.service.TextAnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AnalysisControllerTest {

    @Test
    void aiImproveUpdatesAiTextAndSummary() {
        ProjectRepository repo = mock(ProjectRepository.class);
        GigaChatService ai = mock(GigaChatService.class);
        TextAnalysisService analysis = mock(TextAnalysisService.class);

        Project project = new Project();
        project.setId(1L);
        project.setProcessedText("исходный текст");
        project.setDurationSeconds(90.0);

        TextAnalysisResult analyzed = new TextAnalysisResult();
        analyzed.algorithmicSummary = "старый";

        when(repo.findById(1L)).thenReturn(Optional.of(project));
        when(ai.improveText("исходный текст")).thenReturn("Исправленный текст.");
        when(ai.summarizeText("Исправленный текст.")).thenReturn("Краткое содержание.");
        when(analysis.analyze(eq("Исправленный текст."), any())).thenReturn(analyzed);

        AnalysisController controller = new AnalysisController(repo, ai, analysis);
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        String view = controller.aiImprove(1L, attrs);

        assertEquals("redirect:/projects/1", view);
        assertEquals("Исправленный текст.", project.getAiText());
        assertEquals("Краткое содержание.", project.getAnalysisResult().algorithmicSummary);
        verify(repo).update(project);
        verify(repo).updateAnalysis(1L, analyzed);
    }
}
