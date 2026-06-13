package com.example.audiotext.service;

import com.example.audiotext.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Service
public class FfmpegAudioService implements AudioService {
    private static final Logger log = LoggerFactory.getLogger(FfmpegAudioService.class);
    private static final String FFMPEG_NOT_FOUND_MESSAGE =
            "FFmpeg не найден. Укажите путь к ffmpeg.exe в app.audio.ffmpeg-path или добавьте FFmpeg в PATH";
    private final AppProperties props;
    private final StorageService storage;

    public FfmpegAudioService(AppProperties props, StorageService storage) { this.props = props; this.storage = storage; }
    @Override public boolean isSupported(Path file) { String name = file.getFileName().toString().toLowerCase(); return props.getAudio().getAllowedExtensions().stream().anyMatch(ext -> name.endsWith("." + ext)); }

    @Override
    public Path convertToWav(Path inputFile, Long projectId) {
        if (isVoskReadyWav(inputFile)) {
            log.info("WAV {} already has PCM mono 16 kHz format; FFmpeg conversion is not required", inputFile);
            return inputFile;
        }

        Path out = storage.getConvertedPath(projectId);
        try { Files.createDirectories(out.getParent()); } catch (IOException e) { throw new IllegalStateException("Не удалось подготовить временный каталог для WAV", e); }
        String ffmpeg = configuredFfmpegPath();
        verifyFfmpegAvailable(ffmpeg);
        List<String> cmd = List.of(ffmpeg, "-y", "-i", inputFile.toString(), "-ac", "1", "-ar", "16000", "-sample_fmt", "s16", out.toString());
        try {
            Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            String output = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            int code = p.waitFor();
            if (code != 0) throw new IllegalStateException("Ошибка конвертации FFmpeg: " + conciseProcessOutput(output));
            return out;
        } catch (IOException e) {
            throw new IllegalStateException(FFMPEG_NOT_FOUND_MESSAGE, e);
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException("Конвертация была прервана", e); }
    }

    @Override
    public double getDurationSeconds(Path file) {
        String ffprobe = configuredFfprobePath();
        try {
            Process p = new ProcessBuilder(List.of(ffprobe, "-v", "error", "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1", file.toString())).redirectErrorStream(true).start();
            String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            if (p.waitFor() == 0 && !out.isBlank()) return Double.parseDouble(out);
        } catch (Exception e) { log.warn("ffprobe недоступен: {}", e.getMessage()); }
        return wavDurationSeconds(file);
    }

    private boolean isVoskReadyWav(Path file) {
        if (!file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".wav")) return false;
        try (AudioInputStream stream = AudioSystem.getAudioInputStream(file.toFile())) {
            AudioFormat format = stream.getFormat();
            return AudioFormat.Encoding.PCM_SIGNED.equals(format.getEncoding())
                    && format.getChannels() == 1
                    && format.getSampleSizeInBits() == 16
                    && Math.abs(format.getSampleRate() - 16_000f) < 0.01f
                    && !format.isBigEndian();
        } catch (Exception e) {
            log.warn("Не удалось проверить формат WAV {}: {}", file, e.getMessage());
            return false;
        }
    }

    private double wavDurationSeconds(Path file) {
        if (!file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".wav")) return 0;
        try (AudioInputStream stream = AudioSystem.getAudioInputStream(file.toFile())) {
            AudioFormat format = stream.getFormat();
            if (stream.getFrameLength() <= 0 || format.getFrameRate() <= 0) return 0;
            return stream.getFrameLength() / format.getFrameRate();
        } catch (Exception e) {
            log.warn("Не удалось определить длительность WAV {}: {}", file, e.getMessage());
            return 0;
        }
    }

    private void verifyFfmpegAvailable(String ffmpeg) {
        try {
            Process process = new ProcessBuilder(List.of(ffmpeg, "-version")).redirectErrorStream(true).start();
            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new IllegalStateException(FFMPEG_NOT_FOUND_MESSAGE);
            }
            if (process.exitValue() != 0) throw new IllegalStateException(FFMPEG_NOT_FOUND_MESSAGE);
        } catch (IOException e) {
            throw new IllegalStateException(FFMPEG_NOT_FOUND_MESSAGE, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Проверка FFmpeg была прервана", e);
        }
    }

    private String configuredFfmpegPath() {
        String configured = props.getAudio().getFfmpegPath();
        return configured == null || configured.isBlank() ? "ffmpeg" : configured.trim();
    }

    private String configuredFfprobePath() {
        String configured = props.getAudio().getFfprobePath();
        if (configured != null && !configured.isBlank()) return configured.trim();

        String ffmpeg = configuredFfmpegPath();
        try {
            Path ffmpegPath = Path.of(ffmpeg);
            Path parent = ffmpegPath.getParent();
            if (parent != null) {
                String name = ffmpegPath.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".exe")
                        ? "ffprobe.exe" : "ffprobe";
                return parent.resolve(name).toString();
            }
        } catch (Exception ignored) {
            // A command name that is not a filesystem path falls back to PATH.
        }
        return "ffprobe";
    }

    private String conciseProcessOutput(String output) {
        if (output == null || output.isBlank()) return "процесс завершился с ненулевым кодом";
        String[] lines = output.strip().split("\\R");
        List<String> tail = new ArrayList<>();
        for (int i = Math.max(0, lines.length - 5); i < lines.length; i++) {
            if (!lines[i].isBlank()) tail.add(lines[i].trim());
        }
        String message = String.join(" ", tail);
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}
