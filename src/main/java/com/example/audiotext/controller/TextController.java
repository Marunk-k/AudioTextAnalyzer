package com.example.audiotext.controller;

import com.example.audiotext.model.TranscriptionResult;
import com.example.audiotext.repository.ProjectRepository;
import com.example.audiotext.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class TextController {
    private final ProjectRepository repo; private final TextPostProcessingService post; private final TextAnalysisService analysis; private final CurrentUserService currentUserService;
    public TextController(ProjectRepository repo, TextPostProcessingService post, TextAnalysisService analysis, CurrentUserService currentUserService){this.repo=repo;this.post=post;this.analysis=analysis;this.currentUserService=currentUserService;}
    @PostMapping("/projects/{id}/text/raw") public String saveRaw(@PathVariable Long id,@RequestParam String rawText){return saveText(id,p->p.setRawText(rawText));}
    @PostMapping("/projects/{id}/text/processed") public String saveProcessed(@PathVariable Long id,@RequestParam String processedText){return saveText(id,p->p.setProcessedText(processedText));}
    @PostMapping("/projects/{id}/text/ai") public String saveAi(@PathVariable Long id,@RequestParam String aiText){return saveText(id,p->p.setAiText(aiText));}
    @PostMapping("/projects/{id}/text/manual") public String saveManual(@PathVariable Long id,@RequestParam String manualText){return saveText(id,p->p.setManualText(manualText));}
    @PostMapping("/projects/{id}/text/reprocess") public String reprocess(@PathVariable Long id){ var username=currentUserService.username(); var p=repo.findByIdAndOwner(id,username).orElseThrow(); p.setProcessedText(post.process(p.getRawText(), java.util.List.of(), username).getProcessedText()); recalc(p, username); return "redirect:/projects/"+id; }
    private String saveText(Long id, java.util.function.Consumer<com.example.audiotext.model.Project> c){ var username=currentUserService.username(); var p=repo.findByIdAndOwner(id,username).orElseThrow(); c.accept(p); recalc(p, username); return "redirect:/projects/"+id; }
    private void recalc(com.example.audiotext.model.Project p, String username){ var a=new TranscriptionResult(); a.setDurationSeconds(p.getDurationSeconds()!=null?p.getDurationSeconds():0); p.setAnalysisResult(analysis.analyze(TextVersionSelector.bestTextForAnalysis(p),a,username)); repo.update(p); repo.updateAnalysis(p.getId(),p.getAnalysisResult()); }
}
