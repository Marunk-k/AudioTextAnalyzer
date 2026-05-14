package com.example.audiotext.controller;

import com.example.audiotext.model.ProjectStatus;
import com.example.audiotext.repository.ProjectRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class ProcessingController {
    private final ProjectRepository repo;
    public ProcessingController(ProjectRepository repo){this.repo=repo;}
    @GetMapping("/projects/{id}/status")
    public ResponseEntity<?> status(@PathVariable Long id){
        var p=repo.findById(id).orElseThrow();
        return ResponseEntity.ok(Map.of("id",p.getId(),"status",p.getStatus().name(),"statusLabel",label(p.getStatus()),"error",p.getErrorMessage()==null?"":p.getErrorMessage()));
    }

    private String label(ProjectStatus status) {
        return switch (status) {
            case CREATED -> "Создан";
            case UPLOADED -> "Загружен";
            case CONVERTING -> "Конвертация";
            case TRANSCRIBING -> "Распознавание";
            case POST_PROCESSING -> "Постобработка";
            case ANALYZING -> "Анализ";
            case READY -> "Готово";
            case ERROR -> "Ошибка";
        };
    }
}
