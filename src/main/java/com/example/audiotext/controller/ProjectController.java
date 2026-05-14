package com.example.audiotext.controller;
import com.example.audiotext.repository.ProjectRepository;import com.example.audiotext.service.ProcessingService;import org.springframework.stereotype.Controller;import org.springframework.ui.Model;import org.springframework.web.bind.annotation.*;
@Controller
public class ProjectController {
 private final ProjectRepository repo; private final ProcessingService proc; public ProjectController(ProjectRepository r, ProcessingService p){repo=r;proc=p;}
 @GetMapping("/projects") public String list(Model m){ m.addAttribute("projects",repo.findAll()); return "projects"; }
 @GetMapping("/projects/{id}") public String details(@PathVariable Long id, Model m){ m.addAttribute("project",repo.findById(id).orElseThrow()); return "project-details"; }
 @PostMapping("/projects/{id}/process") public String process(@PathVariable Long id){ proc.processProject(id); return "redirect:/projects/"+id; }
}
