package com.example.audiotext.service;

import com.example.audiotext.config.AppProperties;
import com.example.audiotext.model.ProjectStatus;
import com.example.audiotext.model.TranscriptionResult;
import com.example.audiotext.model.TextAnalysisResult;
import com.example.audiotext.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ProcessingService {
    private static final Logger log = LoggerFactory.getLogger(ProcessingService.class);
    private final ProjectRepository repo;
    private final TranscriptionService tr;
    private final TextPostProcessingService pp;
    private final TextAnalysisService an;
    private final AudioService audio;
    private final AppProperties props;

    public ProcessingService(ProjectRepository repo, TranscriptionService tr, TextPostProcessingService pp,
                             TextAnalysisService an, AudioService audio, AppProperties props) {
        this.repo = repo;
        this.tr = tr;
        this.pp = pp;
        this.an = an;
        this.audio = audio;
        this.props = props;
    }

    public void processProject(Long id, String username) {
        var p = repo.findByIdAndOwner(id, username).orElseThrow();
        Path source = null;
        Path wav = null;
        try {
            p.setErrorMessage(null);
            var audioFile = repo.findOriginalAudioFile(id)
                    .orElseThrow(() -> new IllegalStateException("Исходный аудиофайл не найден в базе данных."));
            source = writeTempSource(id, audioFile.fileName(), audioFile.fileData());
            if (!audio.isSupported(source)) throw new IllegalStateException("Неподдерживаемый формат файла");

            p.setStatus(ProjectStatus.CONVERTING); repo.update(p);
            wav = audio.convertToWav(source, id);

            p.setStatus(ProjectStatus.TRANSCRIBING); repo.update(p);
            double audioDuration = audio.getDurationSeconds(wav);
            var t = tr.transcribe(wav);
            double duration = t.getDurationSeconds() > 0 ? t.getDurationSeconds() : (audioDuration > 0 ? audioDuration : 0);
            p.setRawText(t.getRawText()); p.setDurationSeconds(duration); t.setDurationSeconds(duration);
            repo.saveSegments(id, t.getSegments()); repo.update(p);

            p.setStatus(ProjectStatus.POST_PROCESSING); repo.update(p);
            var pr = pp.process(p.getRawText(), t.getWords(), username); p.setProcessedText(pr.getProcessedText()); repo.update(p);

            p.setStatus(ProjectStatus.ANALYZING); repo.update(p);
            p.setAnalysisResult(analyzeProject(p, t, username)); repo.updateAnalysis(id, p.getAnalysisResult());
            p.setStatus(ProjectStatus.READY); repo.update(p);
        } catch (Exception e) {
            log.error("Processing failed for project {}", id, e);
            p.setStatus(ProjectStatus.ERROR);
            p.setErrorMessage(userMessage(e));
            repo.update(p);
        } finally {
            deleteTemp(wav, id, "WAV");
            deleteTemp(source, id, "исходный файл");
        }
    }

    private String userMessage(Exception error) {
        Throwable current = error;
        while (current != null) {
            if (current.getMessage() != null && !current.getMessage().isBlank()) {
                return current.getMessage();
            }
            current = current.getCause();
        }
        return "Не удалось обработать аудиофайл. Проверьте настройки FFmpeg и модели Vosk.";
    }

    private TextAnalysisResult analyzeProject(com.example.audiotext.model.Project p, TranscriptionResult t, String username) {
        var selected = TextVersionSelector.selectedText(p);
        var result = an.analyze(selected.text(), t, username, p.getRawText());
        result.sourceTextType = selected.type().name();
        return result;
    }

    private Path writeTempSource(Long projectId, String fileName, byte[] data) throws java.io.IOException {
        Path tempDir = Path.of(props.getTempDir());
        Files.createDirectories(tempDir);
        String safeName = fileName == null ? "audio" : Path.of(fileName).getFileName().toString().replaceAll("[^a-zA-Z0-9._-]", "_");
        Path source = Files.createTempFile(tempDir, "project_" + projectId + "_source_", "_" + safeName);
        Files.write(source, data);
        return source;
    }

    private void deleteTemp(Path path, Long projectId, String label) {
        if (path == null) return;
        try {
            Files.deleteIfExists(path);
        } catch (Exception ex) {
            log.warn("Не удалось удалить временный {} для проекта {}: {}", label, projectId, ex.getMessage());
        }
    }
}
