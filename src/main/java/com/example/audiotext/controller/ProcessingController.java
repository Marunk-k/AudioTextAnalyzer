package com.example.audiotext.controller;

import com.example.audiotext.repository.ProjectRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ProcessingController {
    private final ProjectRepository repo;

    public ProcessingController(ProjectRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/projects/{id}/status")
    public ResponseEntity<?> status(@PathVariable Long id) {
        var p = repo.findById(id).orElseThrow();
        return ResponseEntity.ok(Map.of(
                "id", p.getId(),
                "status", p.getStatus().name(),
                "statusLabel", p.getStatus().getDisplayName(),
                "error", p.getErrorMessage() == null ? "" : p.getErrorMessage()
        ));
    }
}
