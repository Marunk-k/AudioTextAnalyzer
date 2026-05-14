package com.example.audiotext.service;

import com.example.audiotext.model.TranscriptionResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextAnalysisServiceTest {
    private final TextAnalysisService service = new TextAnalysisService();

    @Test
    void emptyTextReturnsZeroes() {
        var r = service.analyze("", new TranscriptionResult());
        assertEquals(0, r.wordCount);
        assertEquals(0, r.sentenceCount);
        assertEquals(0, r.paragraphCount);
    }

    @Test
    void countsAndSummaryWork() {
        var r = service.analyze("Привет мир. Привет Java. Это тест резюме.", new TranscriptionResult());
        assertEquals(6, r.wordCount);
        assertTrue(r.sentenceCount >= 3);
        assertTrue(r.uniqueWordCount >= 4);
        assertNotNull(r.algorithmicSummary);
    }
}
