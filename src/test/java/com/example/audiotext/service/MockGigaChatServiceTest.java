package com.example.audiotext.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MockGigaChatServiceTest {
    @Test
    void improveTextHandlesNull(){
        var s=new MockGigaChatService();
        assertDoesNotThrow(() -> s.improveText(null));
    }

    @Test
    void improveTextReturnsNonEmpty(){
        var s=new MockGigaChatService();
        assertFalse(s.improveText("  test  ").isBlank());
    }
}
