package com.example.audiotext.service;

import com.example.audiotext.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Service
public class FfmpegAudioService implements AudioService {
    private static final Logger log = LoggerFactory.getLogger(FfmpegAudioService.class);
    private final AppProperties props;
    private final StorageService storage;

    public FfmpegAudioService(AppProperties props, StorageService storage) { this.props = props; this.storage = storage; }
    @Override public boolean isSupported(Path file) { String name = file.getFileName().toString().toLowerCase(); return props.getAudio().getAllowedExtensions().stream().anyMatch(ext -> name.endsWith("." + ext)); }

    @Override
    public Path convertToWav(Path inputFile, Long projectId) {
        String name = inputFile.getFileName().toString().toLowerCase(); Path out = storage.getConvertedPath(projectId);
        List<String> cmd = List.of(props.getAudio().getFfmpegPath(), "-y", "-i", inputFile.toString(), "-ac", "1", "-ar", "16000", "-sample_fmt", "s16", out.toString());
        try {
            Process p = new ProcessBuilder(cmd).start();
            String stderr = new String(p.getErrorStream().readAllBytes());
            int code = p.waitFor();
            if (code != 0) throw new IllegalStateException("Ошибка конвертации FFmpeg: " + stderr);
            return out;
        } catch (IOException e) {
            if (name.endsWith(".wav")) return inputFile;
            throw new IllegalStateException("FFmpeg не найден. Для не-WAV файлов установите FFmpeg или укажите app.audio.ffmpeg-path.", e);
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException("Конвертация была прервана", e); }
    }

    @Override
    public double getDurationSeconds(Path file) {
        try {
            Process p = new ProcessBuilder("ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "default=noprint_wrappers=1:nokey=1", file.toString()).start();
            String out = new String(p.getInputStream().readAllBytes()).trim();
            if (p.waitFor() == 0 && !out.isBlank()) return Double.parseDouble(out);
        } catch (Exception e) { log.warn("ffprobe недоступен: {}", e.getMessage()); }
        return 0;
    }
}
