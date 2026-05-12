package com.example.audiotext.service;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class TextPostProcessingServiceTest {
 @Test void basic(){ var s=new TextPostProcessingService(); var r=s.process("ну я я изучаю джава", java.util.List.of()); assertTrue(r.getProcessedText().contains("Java")); }
}
