package com.example.audiotext.controller;

import com.example.audiotext.model.Project;
import com.example.audiotext.model.TextAnalysisResult;
import com.example.audiotext.repository.ProjectRepository;
import com.example.audiotext.service.CurrentUserService;
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
    void aiImproveUpdatesAiTextForCurrentOwnerAndRecalculatesAnalysis() {
        ProjectRepository repo = mock(ProjectRepository.class);
        GigaChatService ai = mock(GigaChatService.class);
        TextAnalysisService analysis = mock(TextAnalysisService.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);

        Project project = new Project();
        project.setId(1L);
        project.setRawText("сырой текст");
        project.setProcessedText("очищенный черновик");
        project.setDurationSeconds(90.0);

        TextAnalysisResult analyzed = new TextAnalysisResult();

        when(currentUserService.username()).thenReturn("user");
        when(repo.findByIdAndOwner(1L, "user")).thenReturn(Optional.of(project));
        when(ai.improveText("очищенный черновик")).thenReturn("AI текст.");
        when(analysis.analyze(eq("AI текст."), any(), eq("user"), eq("сырой текст"))).thenReturn(analyzed);

        AnalysisController controller = new AnalysisController(repo, ai, analysis, currentUserService);
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        String view = controller.aiImprove(1L, attrs);

        assertEquals("redirect:/projects/1", view);
        assertEquals("AI текст.", project.getAiText());
        assertEquals("AI", project.getAnalysisResult().sourceTextType);
        verify(repo).findByIdAndOwner(1L, "user");
        verify(repo, never()).findById(1L);
        verify(ai).improveText("очищенный черновик");
        verify(repo).update(project);
        verify(repo).updateAnalysis(1L, analyzed);
    }
}
