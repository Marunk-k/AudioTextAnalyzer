package com.example.audiotext.controller;

import com.example.audiotext.repository.ProjectRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private final ProjectRepository repo;
    public HomeController(ProjectRepository repo){this.repo=repo;}

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("recentProjects", repo.findAll().stream().limit(3).toList());
        return "index";
    }

    @GetMapping("/workspace")
    public String workspace(Model model) {
        model.addAttribute("recentProjects", repo.findAll().stream().limit(5).toList());
        return "workspace";
    }

    @GetMapping("/settings")
    public String settings() { return "settings"; }

    @GetMapping("/help")
    public String help() { return "help"; }
}
