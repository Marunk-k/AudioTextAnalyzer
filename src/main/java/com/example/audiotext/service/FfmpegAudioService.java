package com.example.audiotext.service;

import com.example.audiotext.config.AppProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Service
public class FfmpegAudioService implements AudioService {
    private final AppProperties props;
    private final StorageService storage;

    public FfmpegAudioService(AppProperties props, StorageService storage) {
        this.props = props;
        this.storage = storage;
    }

    @Override
    public boolean isSupported(Path file) {
        String name = file.getFileName().toString().toLowerCase();
        return props.getAudio().getAllowedExtensions().stream().anyMatch(ext -> name.endsWith("." + ext));
    }

    @Override
    public Path convertToWav(Path inputFile, Long projectId) {
        String name = inputFile.getFileName().toString().toLowerCase();
        if (name.endsWith(".wav")) return inputFile;
        Path out = storage.getConvertedPath(projectId);
        List<String> cmd = List.of(props.getAudio().getFfmpegPath(), "-y", "-i", inputFile.toString(), "-ac", "1", "-ar", "16000", "-sample_fmt", "s16", out.toString());
        try {
            Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            int code = p.waitFor();
            if (code != 0) throw new IllegalStateException("Ошибка конвертации FFmpeg (code=" + code + ")");
            return out;
        } catch (IOException e) {
            throw new IllegalStateException("FFmpeg не найден. Установите FFmpeg или укажите путь в application.yml.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Конвертация была прервана", e);
        }
    }

    @Override
    public double getDurationSeconds(Path file) { return 0; }
}
