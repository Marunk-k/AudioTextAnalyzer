package com.example.audiotext.controller;

import org.springframework.beans.factory.annotation.Autowired;
import com.example.audiotext.repository.ProjectRepository;
import com.example.audiotext.service.CurrentUserService;
import com.example.audiotext.service.GigaChatService;
import com.example.audiotext.service.TextAnalysisService;
import com.example.audiotext.service.TextVersionSelector;
import com.example.audiotext.model.TranscriptionResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AnalysisController {
    private final ProjectRepository repo; private final GigaChatService ai; private final CurrentUserService currentUserService;
    private final TextAnalysisService analysis;
    public AnalysisController(ProjectRepository repo, GigaChatService ai, TextAnalysisService analysis){this(repo, ai, analysis, null);}
    @Autowired
    public AnalysisController(ProjectRepository repo, GigaChatService ai, TextAnalysisService analysis, CurrentUserService currentUserService){this.repo=repo;this.ai=ai;this.analysis=analysis;this.currentUserService=currentUserService;}

    @PostMapping("/projects/{id}/text/ai-improve")
    public String aiImprove(@PathVariable Long id, RedirectAttributes redirectAttributes){
        String username = username();
        var p = username == null ? repo.findById(id).orElseThrow() : repo.findByIdAndOwner(id, username).orElseThrow();
        String base = TextVersionSelector.hasText(p.getProcessedText()) ? p.getProcessedText() : p.getRawText();
        if(base==null || base.isBlank()){
            redirectAttributes.addFlashAttribute("warning", "Нет текста для AI-постобработки.");
            return "redirect:/projects/"+id;
        }
        try {
            String aiText = ai.improveText(base);
            p.setAiText(aiText);
            var analysisContext = new TranscriptionResult();
            analysisContext.setDurationSeconds(p.getDurationSeconds() != null ? p.getDurationSeconds() : 0);
            var analysisResult = username == null ? analysis.analyze(TextVersionSelector.bestTextForAnalysis(p), analysisContext) : analysis.analyze(TextVersionSelector.bestTextForAnalysis(p), analysisContext, username, p.getRawText());
            analysisResult.algorithmicSummary = ai.summarizeText(aiText);
            p.setAnalysisResult(analysisResult);
            repo.update(p);
            repo.updateAnalysis(id, analysisResult);
            redirectAttributes.addFlashAttribute("success", "AI-постобработка выполнена.");
        } catch (Exception ex){
            redirectAttributes.addFlashAttribute("warning", "AI-постобработка временно недоступна или не настроена. Можно продолжить работу с текущим текстом.");
        }
        return "redirect:/projects/"+id;
    }
    private String username(){ return currentUserService == null ? null : currentUserService.username(); }
}
