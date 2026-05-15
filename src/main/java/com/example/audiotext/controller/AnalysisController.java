package com.example.audiotext.controller;

import com.example.audiotext.repository.ProjectRepository;
import com.example.audiotext.service.GigaChatService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AnalysisController {
    private final ProjectRepository repo; private final GigaChatService ai;
    public AnalysisController(ProjectRepository repo, GigaChatService ai){this.repo=repo;this.ai=ai;}

    @PostMapping("/projects/{id}/text/ai-improve")
    public String aiImprove(@PathVariable Long id, RedirectAttributes redirectAttributes){
        var p=repo.findById(id).orElseThrow();
        String base=(p.getProcessedText()!=null && !p.getProcessedText().isBlank())?p.getProcessedText():p.getRawText();
        try {
            p.setAiText(ai.improveText(base));
            repo.update(p);
        } catch (Exception ex){
            redirectAttributes.addFlashAttribute("warning", "AI-постобработка временно недоступна. Попробуйте позже.");
        }
        return "redirect:/projects/"+id;
    }
}
