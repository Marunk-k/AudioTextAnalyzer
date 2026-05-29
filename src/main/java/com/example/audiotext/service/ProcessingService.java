package com.example.audiotext.service;

import com.example.audiotext.model.ProjectStatus;
import com.example.audiotext.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.audiotext.service.TextVersionSelector;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ProcessingService {
    private static final Logger log = LoggerFactory.getLogger(ProcessingService.class);
    private final ProjectRepository repo; private final TranscriptionService tr; private final TextPostProcessingService pp; private final TextAnalysisService an; private final AudioService audio;
    public ProcessingService(ProjectRepository repo, TranscriptionService tr, TextPostProcessingService pp, TextAnalysisService an, AudioService audio){this.repo=repo;this.tr=tr;this.pp=pp;this.an=an;this.audio=audio;}
    public void processProject(Long id, String username){ var p=repo.findByIdAndOwner(id, username).orElseThrow(); Path source=null; Path wav=null; try{
        p.setErrorMessage(null);
        source=Path.of(p.getOriginalFilePath());
        if (!Files.exists(source)) throw new IllegalStateException("Исходный файл не найден.");
        if(!audio.isSupported(source)) throw new IllegalStateException("Неподдерживаемый формат файла");
        p.setStatus(ProjectStatus.CONVERTING); repo.update(p);
        wav=audio.convertToWav(source,id); p.setConvertedFilePath(wav.toString()); repo.update(p);

        p.setStatus(ProjectStatus.TRANSCRIBING); repo.update(p);
        double audioDuration = audio.getDurationSeconds(wav);
        var t=tr.transcribe(wav);
        double duration = t.getDurationSeconds() > 0 ? t.getDurationSeconds() : (audioDuration > 0 ? audioDuration : 0);
        p.setRawText(t.getRawText()); p.setDurationSeconds(duration); t.setDurationSeconds(duration); repo.saveSegments(id, t.getSegments()); repo.update(p);

        p.setStatus(ProjectStatus.POST_PROCESSING); repo.update(p); var pr=pp.process(p.getRawText(), t.getWords(), username); p.setProcessedText(pr.getProcessedText()); repo.update(p);
        p.setStatus(ProjectStatus.ANALYZING); repo.update(p); p.setAnalysisResult(an.analyze(TextVersionSelector.bestTextForAnalysis(p),t,username,p.getRawText())); repo.updateAnalysis(id,p.getAnalysisResult());
        p.setStatus(ProjectStatus.READY); repo.update(p);
    }catch(Exception e){ log.error("Processing failed for project {}", id, e); p.setStatus(ProjectStatus.ERROR); p.setErrorMessage(e.getMessage()); repo.update(p);} finally { try { if (wav != null && !wav.equals(source)) Files.deleteIfExists(wav); } catch (Exception ex) { log.warn("Не удалось удалить временный WAV для проекта {}: {}", id, ex.getMessage()); } } }
}
