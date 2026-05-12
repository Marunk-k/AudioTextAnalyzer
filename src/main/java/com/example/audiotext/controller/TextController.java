package com.example.audiotext.controller;

import com.example.audiotext.repository.ProjectRepository;
import com.example.audiotext.service.TextAnalysisService;
import com.example.audiotext.service.TextPostProcessingService;
import com.example.audiotext.model.TranscriptionResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class TextController {
    private final ProjectRepository repo; private final TextPostProcessingService post; private final TextAnalysisService analysis;
    public TextController(ProjectRepository repo, TextPostProcessingService post, TextAnalysisService analysis){this.repo=repo;this.post=post;this.analysis=analysis;}
    @PostMapping("/projects/{id}/text/processed")
    public String saveProcessed(@PathVariable Long id, @RequestParam String processedText){
        var p=repo.findById(id).orElseThrow(); p.setProcessedText(processedText); p.setAnalysisResult(analysis.analyze(processedText,new TranscriptionResult())); repo.update(p); return "redirect:/projects/"+id;
    }
    @PostMapping("/projects/{id}/text/reprocess")
    public String reprocess(@PathVariable Long id){ var p=repo.findById(id).orElseThrow(); p.setProcessedText(post.process(p.getRawText(), java.util.List.of()).getProcessedText()); repo.update(p); return "redirect:/projects/"+id; }
}
