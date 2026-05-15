package com.example.audiotext.service;

import com.example.audiotext.model.ProjectStatus;
import com.example.audiotext.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ProcessingService {
    private static final Logger log = LoggerFactory.getLogger(ProcessingService.class);
    private final ProjectRepository repo; private final TranscriptionService tr; private final TextPostProcessingService pp; private final TextAnalysisService an; private final AudioService audio;
    public ProcessingService(ProjectRepository repo, TranscriptionService tr, TextPostProcessingService pp, TextAnalysisService an, AudioService audio){this.repo=repo;this.tr=tr;this.pp=pp;this.an=an;this.audio=audio;}
    public void processProject(Long id){ var p=repo.findById(id).orElseThrow(); try{
        p.setErrorMessage(null);
        Path source=Path.of(p.getOriginalFilePath());
        if (!Files.exists(source)) throw new IllegalStateException("Исходный файл не найден.");
        if(!audio.isSupported(source)) throw new IllegalStateException("Неподдерживаемый формат файла");
        p.setStatus(ProjectStatus.CONVERTING); repo.update(p);
        Path wav=audio.convertToWav(source,id); p.setConvertedFilePath(wav.toString()); repo.update(p);

        p.setStatus(ProjectStatus.TRANSCRIBING); repo.update(p);
        double audioDuration = audio.getDurationSeconds(wav);
        var t=tr.transcribe(wav);
        double duration = t.getDurationSeconds() > 0 ? t.getDurationSeconds() : (audioDuration > 0 ? audioDuration : 0);
        p.setRawText(t.getRawText()); p.setDurationSeconds(duration); t.setDurationSeconds(duration); repo.saveSegments(id, t.getSegments()); repo.update(p);

        p.setStatus(ProjectStatus.POST_PROCESSING); repo.update(p); var pr=pp.process(p.getRawText(), t.getWords()); p.setProcessedText(pr.getProcessedText()); repo.update(p);
        p.setStatus(ProjectStatus.ANALYZING); repo.update(p); p.setAnalysisResult(an.analyze(p.getProcessedText(),t)); repo.updateAnalysis(id,p.getAnalysisResult());
        p.setStatus(ProjectStatus.READY); repo.update(p);
    }catch(Exception e){ log.error("Processing failed for project {}", id, e); p.setStatus(ProjectStatus.ERROR); p.setErrorMessage(e.getMessage()); repo.update(p);} }
}
