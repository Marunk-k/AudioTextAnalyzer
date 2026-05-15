package com.example.audiotext.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextPostProcessingServiceTest {
    private final TextPostProcessingService service = new TextPostProcessingService();

    @Test
    void noAggressiveSentenceSplitAndFinalDot() {
        var result = service.process("джо ланс дэйл левитация на мгновение наступила тьма а потом стало светло", List.of());
        assertFalse(result.getProcessedText().contains("левитация на. Мгновение"));
        assertTrue(result.getProcessedText().endsWith("."));
        assertTrue(Character.isUpperCase(result.getProcessedText().charAt(0)));
    }

    @Test
    void shortTextGetsFinalDot() {
        var result = service.process("короткий текст", List.of());
        assertTrue(result.getProcessedText().trim().endsWith("."));
    }

    @Test
    void removesFillersDuplicatesAndReplacesTerms() {
        var result = service.process("ну я я как бы изучаю джава и воск", List.of());
        assertFalse(result.getProcessedText().toLowerCase().contains("ну"));
        assertFalse(result.getProcessedText().toLowerCase().contains("как бы"));
        assertFalse(result.getProcessedText().toLowerCase().contains("я я"));
        assertTrue(result.getProcessedText().contains("Java"));
        assertTrue(result.getProcessedText().contains("Vosk"));
    }
}
