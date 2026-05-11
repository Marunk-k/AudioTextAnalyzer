package com.example.audiotext.service;
import com.example.audiotext.model.*;import com.example.audiotext.repository.ProjectRepository;import org.springframework.stereotype.Service;
@Service
public class ProcessingService {
 private final ProjectRepository repo; private final TranscriptionService tr; private final TextPostProcessingService pp; private final TextAnalysisService an;
 public ProcessingService(ProjectRepository repo, TranscriptionService tr, TextPostProcessingService pp, TextAnalysisService an){this.repo=repo;this.tr=tr;this.pp=pp;this.an=an;}
 public void processProject(Long id){ var p=repo.findById(id).orElseThrow(); try{ p.setStatus(ProjectStatus.TRANSCRIBING); repo.update(p); var t=tr.transcribe(java.nio.file.Path.of(p.getOriginalFilePath())); p.setRawText(t.getRawText()); p.setDurationSeconds(t.getDurationSeconds()); p.setStatus(ProjectStatus.POST_PROCESSING); repo.update(p); var pr=pp.process(p.getRawText(), t.getWords()); p.setProcessedText(pr.getProcessedText()); p.setStatus(ProjectStatus.ANALYZING); repo.update(p); p.setAnalysisResult(an.analyze(p.getProcessedText(),t)); p.setStatus(ProjectStatus.READY); repo.update(p);}catch(Exception e){ p.setStatus(ProjectStatus.ERROR); p.setErrorMessage(e.getMessage()); repo.update(p);} }
}
