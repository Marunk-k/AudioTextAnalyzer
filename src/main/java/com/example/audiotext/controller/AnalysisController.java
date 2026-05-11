package com.example.audiotext.controller;

import com.example.audiotext.repository.ProjectRepository;
import com.example.audiotext.service.GigaChatService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class AnalysisController {
    private final ProjectRepository repo; private final GigaChatService ai;
    public AnalysisController(ProjectRepository repo, GigaChatService ai){this.repo=repo;this.ai=ai;}

    @PostMapping("/projects/{id}/text/ai-improve")
    public String aiImprove(@PathVariable Long id){
        var p=repo.findById(id).orElseThrow();
        String base=(p.getProcessedText()!=null && !p.getProcessedText().isBlank())?p.getProcessedText():p.getRawText();
        p.setAiText(ai.improveText(base));
        repo.update(p);
        return "redirect:/projects/"+id;
    }
}
