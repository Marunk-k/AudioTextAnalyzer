package com.example.audiotext.service;

import com.example.audiotext.model.ProjectStatus;
import com.example.audiotext.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class ProcessingService {
 private final ProjectRepository repo; private final TranscriptionService tr; private final TextPostProcessingService pp; private final TextAnalysisService an; private final AudioService audio;
 public ProcessingService(ProjectRepository repo, TranscriptionService tr, TextPostProcessingService pp, TextAnalysisService an, AudioService audio){this.repo=repo;this.tr=tr;this.pp=pp;this.an=an;this.audio=audio;}
 public void processProject(Long id){ var p=repo.findById(id).orElseThrow(); try{
   Path source=Path.of(p.getOriginalFilePath()); if(!audio.isSupported(source)) throw new IllegalStateException("Неподдерживаемый формат файла");
   p.setStatus(ProjectStatus.CONVERTING); repo.update(p); Path wav=audio.convertToWav(source,id);
   p.setStatus(ProjectStatus.TRANSCRIBING); repo.update(p); var t=tr.transcribe(wav); p.setRawText(t.getRawText()); p.setDurationSeconds(t.getDurationSeconds()); repo.saveSegments(id, t.getSegments());
   p.setStatus(ProjectStatus.POST_PROCESSING); repo.update(p); var pr=pp.process(p.getRawText(), t.getWords()); p.setProcessedText(pr.getProcessedText());
   p.setStatus(ProjectStatus.ANALYZING); repo.update(p); p.setAnalysisResult(an.analyze(p.getProcessedText(),t));
   p.setStatus(ProjectStatus.READY); repo.update(p);
 }catch(Exception e){ p.setStatus(ProjectStatus.ERROR); p.setErrorMessage(e.getMessage()); repo.update(p);} }
}
