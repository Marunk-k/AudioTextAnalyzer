package com.example.audiotext.controller;

import com.example.audiotext.repository.ProjectRepository;
import com.example.audiotext.service.GigaChatService;
import com.example.audiotext.service.TextAnalysisService;
import com.example.audiotext.model.TranscriptionResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AnalysisController {
    private final ProjectRepository repo; private final GigaChatService ai;
    private final TextAnalysisService analysis;
    public AnalysisController(ProjectRepository repo, GigaChatService ai, TextAnalysisService analysis){this.repo=repo;this.ai=ai;this.analysis=analysis;}

    @PostMapping("/projects/{id}/text/ai-improve")
    public String aiImprove(@PathVariable Long id, RedirectAttributes redirectAttributes){
        var p=repo.findById(id).orElseThrow();
        String base=(p.getProcessedText()!=null && !p.getProcessedText().isBlank())?p.getProcessedText():p.getRawText();
        if(base==null || base.isBlank()){
            redirectAttributes.addFlashAttribute("warning", "Нет текста для AI-постобработки.");
            return "redirect:/projects/"+id;
        }
        try {
            String aiText = ai.improveText(base);
            p.setAiText(aiText);
            var analysisContext = new TranscriptionResult();
            analysisContext.setDurationSeconds(p.getDurationSeconds() != null ? p.getDurationSeconds() : 0);
            var analysisResult = analysis.analyze(aiText, analysisContext);
            analysisResult.algorithmicSummary = ai.summarizeText(aiText);
            p.setAnalysisResult(analysisResult);
            repo.update(p);
            repo.updateAnalysis(id, analysisResult);
            redirectAttributes.addFlashAttribute("success", "AI-постобработка выполнена.");
        } catch (Exception ex){
            redirectAttributes.addFlashAttribute("warning", "AI-постобработка временно недоступна. Попробуйте позже.");
        }
        return "redirect:/projects/"+id;
    }
}
