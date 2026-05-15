package com.example.audiotext.model;

public enum ProjectStatus {
    CREATED,
    UPLOADED,
    CONVERTING,
    TRANSCRIBING,
    POST_PROCESSING,
    ANALYZING,
    READY,
    ERROR;

    public boolean isProcessing() {
        return this == CONVERTING
                || this == TRANSCRIBING
                || this == POST_PROCESSING
                || this == ANALYZING;
    }

    public String getDisplayLabel() {
        return switch (this) {
            case CREATED -> "Создано";
            case UPLOADED -> "Файл загружен";
            case CONVERTING -> "Конвертация";
            case TRANSCRIBING -> "Распознавание";
            case POST_PROCESSING -> "Постобработка";
            case ANALYZING -> "Анализ";
            case READY -> "Готово";
            case ERROR -> "Ошибка";
        };
    }

    public String getProcessingStage() {
        return switch (this) {
            case CONVERTING -> "Конвертация";
            case TRANSCRIBING -> "Распознавание";
            case POST_PROCESSING -> "Постобработка";
            case ANALYZING -> "Анализ";
            default -> "";
        };
    }
}
