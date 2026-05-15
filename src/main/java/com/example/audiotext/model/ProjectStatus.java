package com.example.audiotext.model;
public enum ProjectStatus {
    CREATED("Создан"),
    UPLOADED("Файл загружен"),
    CONVERTING("Конвертация"),
    TRANSCRIBING("Распознавание"),
    POST_PROCESSING("Постобработка"),
    ANALYZING("Анализ"),
    READY("Готово"),
    ERROR("Ошибка");

    private final String displayName;
    ProjectStatus(String displayName){this.displayName=displayName;}
    public String getDisplayName(){return displayName;}
    public boolean isProcessing(){
        return this==CONVERTING||this==TRANSCRIBING||this==POST_PROCESSING||this==ANALYZING;
    }
}
