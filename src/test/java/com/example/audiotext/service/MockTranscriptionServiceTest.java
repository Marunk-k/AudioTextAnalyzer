package com.example.audiotext.service;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MockTranscriptionServiceTest {
    @Test
    void returnsMultipleSegments() {
        var service = new MockTranscriptionService();
        var result = service.transcribe(Path.of("sample.wav"));
        assertTrue(result.getSegments().size() >= 4);
    }
}
