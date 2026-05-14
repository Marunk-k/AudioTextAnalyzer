package com.example.audiotext.service;

import com.example.audiotext.model.WordInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextPostProcessingServiceTest {
    private final TextPostProcessingService service = new TextPostProcessingService();

    @Test
    void removesDuplicatesAndFillersAndReplacesTerms() {
        var result = service.process("ну я я изучаю джава как бы воск", List.of());
        assertFalse(result.getProcessedText().toLowerCase().contains("ну"));
        assertTrue(result.getProcessedText().contains("Java"));
        assertTrue(result.getProcessedText().contains("Vosk"));
    }

    @Test
    void addsPunctuationFromPauses() {
        var words = List.of(new WordInfo("привет",0,0.1,0.9), new WordInfo("мир",1.1,1.2,0.9));
        var result = service.process("привет мир", words);
        assertTrue(result.getProcessedText().contains("."));
    }
}
