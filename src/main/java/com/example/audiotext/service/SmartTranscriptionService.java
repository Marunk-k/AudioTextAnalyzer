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

    @Override
    public TranscriptionResult transcribe(Path wavFile) {
        boolean voskEnabled = props.getVosk().isEnabled();
        Path modelPath = Path.of(props.getVosk().getModelPath() == null ? "" : props.getVosk().getModelPath());
        boolean modelExists = !modelPath.toString().isBlank() && Files.exists(modelPath);

        if (voskEnabled && modelExists) {
            return vosk.transcribe(wavFile);
        }
        if (props.getProcessing().isUseMockTranscriptionIfVoskUnavailable()) {
            return mock.transcribe(wavFile);
        }
        throw new IllegalStateException("Модель Vosk не найдена, а mock-режим отключён.");
    }
}
