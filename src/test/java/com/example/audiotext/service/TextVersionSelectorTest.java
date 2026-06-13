package com.example.audiotext.service;

import com.example.audiotext.model.Project;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextVersionSelectorTest {
    @Test
    void selectsBestAvailableTextForFinalResult() {
        Project project = new Project();
        project.setRawText("Исходный текст");
        assertEquals("Исходный текст", TextVersionSelector.bestTextForAnalysis(project));

        project.setProcessedText("Предобработанный текст");
        assertEquals("Предобработанный текст", TextVersionSelector.bestTextForAnalysis(project));

        project.setAiText("AI-текст");
        assertEquals("AI-текст", TextVersionSelector.bestTextForAnalysis(project));

        project.setManualText("Ручной финальный текст");
        assertEquals("Ручной финальный текст", TextVersionSelector.bestTextForAnalysis(project));
    }

    @Test
    void ignoresBlankHigherPriorityVersions() {
        Project project = new Project();
        project.setRawText("Исходный текст");
        project.setProcessedText("Предобработанный текст");
        project.setAiText("   ");
        project.setManualText("");

        assertEquals("Предобработанный текст", TextVersionSelector.bestTextForAnalysis(project));
    }
}
