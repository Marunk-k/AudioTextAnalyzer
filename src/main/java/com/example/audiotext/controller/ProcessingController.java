package com.example.audiotext.controller;

import com.example.audiotext.model.ProjectStatus;
import com.example.audiotext.repository.ProjectRepository;
import com.example.audiotext.service.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class ProcessingController {
    private final ProjectRepository repo; private final CurrentUserService currentUserService;
    public ProcessingController(ProjectRepository repo, CurrentUserService currentUserService){this.repo=repo;this.currentUserService=currentUserService;}
    @GetMapping("/projects/{id}/status")
    public ResponseEntity<?> status(@PathVariable Long id){
        var p=repo.findByIdAndOwner(id, currentUserService.username()).orElseThrow();
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
