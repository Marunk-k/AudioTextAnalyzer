package com.example.audiotext.service;
import com.example.audiotext.model.TranscriptionResult;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class TextAnalysisServiceTest {
 @Test void counts(){ var s=new TextAnalysisService(); var r=s.analyze("Привет мир. Привет Java.", new TranscriptionResult()); assertEquals(4,r.wordCount); assertTrue(r.uniqueWordCount>=3); }
}
