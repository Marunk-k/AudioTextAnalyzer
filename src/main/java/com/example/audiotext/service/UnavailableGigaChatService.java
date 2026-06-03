package com.example.audiotext.service;

public class UnavailableGigaChatService implements GigaChatService {
    private static final String MESSAGE = "GigaChat недоступен: сервис отключён или не заданы credentials.";

    @Override
    public String improveText(String text) {
        throw new IllegalStateException(MESSAGE);
    }

    @Override
    public String summarizeText(String text) {
        throw new IllegalStateException(MESSAGE);
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
