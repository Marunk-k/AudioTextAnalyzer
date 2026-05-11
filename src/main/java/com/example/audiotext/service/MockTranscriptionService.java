package com.example.audiotext.service;
import com.example.audiotext.model.*;import org.springframework.core.io.ClassPathResource;import org.springframework.stereotype.Service;import java.nio.file.Path;import java.nio.charset.StandardCharsets;
@Service
public class MockTranscriptionService implements TranscriptionService {
 public TranscriptionResult transcribe(Path wavFile){ try{ String t=new String(new ClassPathResource("samples/sample_transcription.txt").getInputStream().readAllBytes(), StandardCharsets.UTF_8); var r=new TranscriptionResult(); r.setRawText(t); r.setDurationSeconds(180); r.getSegments().add(new TranscriptionSegment(0,15,t,0.92)); return r; }catch(Exception e){ throw new RuntimeException(e);} }
}
