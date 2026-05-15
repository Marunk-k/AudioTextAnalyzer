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

    public String getDisplayName() {
        return switch (this) {
            case CREATED -> "Создан";
            case UPLOADED -> "Файл загружен";
            case CONVERTING -> "Конвертация";
            case TRANSCRIBING -> "Распознавание";
            case POST_PROCESSING -> "Постобработка";
            case ANALYZING -> "Анализ";
            case READY -> "Готово";
            case ERROR -> "Ошибка";
        };
    }
}
