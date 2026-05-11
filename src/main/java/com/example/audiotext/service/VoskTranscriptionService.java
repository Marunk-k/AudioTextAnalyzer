package com.example.audiotext.service;

import com.example.audiotext.model.TranscriptionResult;
import com.example.audiotext.model.TranscriptionSegment;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class VoskTranscriptionService {
    public TranscriptionResult transcribe(Path wavFile) {
        // Заглушка под реальную интеграцию Vosk.
        // Для демонстрации возвращаем структурированный результат и пометку,
        // что режим Vosk должен быть донастроен моделью.
        TranscriptionResult result = new TranscriptionResult();
        String text = "[Vosk-mode placeholder] Модель найдена, интеграция ожидает полноценного подключения.";
        result.setRawText(text);
        result.setDurationSeconds(60);
        result.getSegments().add(new TranscriptionSegment(0, 10, text, 0.75));
        return result;
    }
}
