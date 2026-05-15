package com.example.audiotext.service;

import com.example.audiotext.model.TranscriptionResult;
import com.example.audiotext.model.TranscriptionSegment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@Service
public class MockTranscriptionService implements TranscriptionService {
 public TranscriptionResult transcribe(Path wavFile){
     try{
         String t=new String(new ClassPathResource("samples/sample_transcription.txt").getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
         var r=new TranscriptionResult();
         r.setRawText(t);
         r.setDurationSeconds(180);
         String[] parts=t.split("(?<=[.!?])\\s+");
         int segmentCount=Math.min(Math.max(parts.length,4),6);
         double step=r.getDurationSeconds()/segmentCount;
         for(int i=0;i<segmentCount;i++){
             String text = i < parts.length ? parts[i] : parts[parts.length-1];
             double start=i*step;
             double end=(i+1)*step;
             double confidence=0.85 + (i % 3) * 0.05;
             r.getSegments().add(new TranscriptionSegment(start,end,text,Math.min(confidence,0.95)));
         }
         return r;
     }catch(Exception e){ throw new RuntimeException(e);} }
}
