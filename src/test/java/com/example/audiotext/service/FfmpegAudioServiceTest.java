package com.example.audiotext.service;

import com.example.audiotext.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FfmpegAudioServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void usesConfiguredExecutableAsSingleProcessBuilderArgument() throws Exception {
        Path binDir = Files.createDirectories(tempDir.resolve("ffmpeg tools"));
        Path executable = binDir.resolve("ffmpeg");
        Path argumentsFile = tempDir.resolve("arguments.txt");
        Files.writeString(executable, """
                #!/bin/sh
                if [ "$1" = "-version" ]; then
                  echo "ffmpeg test"
                  exit 0
                fi
                printf '%s\\n' "$@" > '%s'
                last=''
                for arg in "$@"; do last="$arg"; done
                touch "$last"
                """.formatted(argumentsFile));
        assertTrue(executable.toFile().setExecutable(true));

        Path output = tempDir.resolve("converted output.wav");
        StorageService storage = mock(StorageService.class);
        when(storage.getConvertedPath(42L)).thenReturn(output);
        AppProperties properties = properties(executable.toString());
        FfmpegAudioService service = new FfmpegAudioService(properties, storage);
        Path input = Files.writeString(tempDir.resolve("input audio.mp3"), "audio");

        assertEquals(output, service.convertToWav(input, 42L));
        assertTrue(Files.exists(output));
        List<String> arguments = Files.readAllLines(argumentsFile);
        assertEquals(input.toString(), arguments.get(arguments.indexOf("-i") + 1));
        assertEquals(output.toString(), arguments.get(arguments.size() - 1));
    }

    @Test
    void reportsClearMessageWhenFfmpegIsUnavailable() throws Exception {
        StorageService storage = mock(StorageService.class);
        when(storage.getConvertedPath(1L)).thenReturn(tempDir.resolve("converted.wav"));
        AppProperties properties = properties(tempDir.resolve("missing ffmpeg").toString());
        FfmpegAudioService service = new FfmpegAudioService(properties, storage);
        Path input = Files.writeString(tempDir.resolve("input.mp3"), "audio");

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> service.convertToWav(input, 1L));

        assertEquals("FFmpeg не найден. Укажите путь к ffmpeg.exe в app.audio.ffmpeg-path или добавьте FFmpeg в PATH",
                error.getMessage());
    }

    @Test
    void derivesFfprobeNextToConfiguredFfmpeg() throws Exception {
        Path binDir = Files.createDirectories(tempDir.resolve("bin"));
        Path ffmpeg = Files.writeString(binDir.resolve("ffmpeg"), "#!/bin/sh\nexit 0\n");
        Path ffprobe = Files.writeString(binDir.resolve("ffprobe"), """
                #!/bin/sh
                echo "12.5"
                """);
        assertTrue(ffmpeg.toFile().setExecutable(true));
        assertTrue(ffprobe.toFile().setExecutable(true));

        FfmpegAudioService service = new FfmpegAudioService(properties(ffmpeg.toString()), mock(StorageService.class));

        assertEquals(12.5, service.getDurationSeconds(tempDir.resolve("audio.wav")));
    }

    private AppProperties properties(String ffmpegPath) {
        AppProperties properties = new AppProperties();
        properties.getAudio().setFfmpegPath(ffmpegPath);
        properties.getAudio().setAllowedExtensions(List.of("wav", "mp3"));
        return properties;
    }
}
