package com.example.audiotext.service;

import com.example.audiotext.config.AppProperties;
import com.example.audiotext.model.TranscriptionResult;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Primary
public class SmartTranscriptionService implements TranscriptionService {
    private final AppProperties props;
    private final MockTranscriptionService mock;
    private final VoskTranscriptionService vosk;

    public SmartTranscriptionService(AppProperties props, MockTranscriptionService mock, VoskTranscriptionService vosk) {
        this.props = props;
        this.mock = mock;
        this.vosk = vosk;
    }


    public String currentMode() {
        String mp = props.getVosk().getModelPath();
        boolean modelExists = mp != null && !mp.isBlank() && Files.exists(Path.of(mp));
        if (props.getVosk().isEnabled() && modelExists) return "VOSK";
        return props.getProcessing().isUseMockTranscriptionIfVoskUnavailable() ? "MOCK (dev)" : "VOSK недоступен";
    }

    @Override
    public TranscriptionResult transcribe(Path wavFile) {
        boolean voskEnabled = props.getVosk().isEnabled();
        String mp = props.getVosk().getModelPath();
        boolean modelExists = mp != null && !mp.isBlank() && Files.exists(Path.of(mp));

        if (voskEnabled && modelExists) {
            return vosk.transcribe(wavFile);
        }
        if (props.getProcessing().isUseMockTranscriptionIfVoskUnavailable()) {
            return mock.transcribe(wavFile);
        }
        throw new IllegalStateException("Модель Vosk не найдена, а mock-режим отключён.");
    }
}
