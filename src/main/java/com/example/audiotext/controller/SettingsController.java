package com.example.audiotext.controller;

import com.example.audiotext.repository.UserDictionaryRepository;
import com.example.audiotext.service.CurrentUserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class SettingsController {
    private final UserDictionaryRepository repo; private final CurrentUserService currentUserService;
    public SettingsController(UserDictionaryRepository repo, CurrentUserService currentUserService){this.repo=repo;this.currentUserService=currentUserService;}
    @GetMapping("/settings") public String page(Model model){ model.addAttribute("entries", repo.findByUserLogin(currentUserService.username())); return "settings"; }
    @PostMapping("/settings/fillers") public String add(@RequestParam String value){ if(value!=null&&!value.isBlank()) repo.add(currentUserService.username(), value); return "redirect:/settings"; }
    @PostMapping("/settings/fillers/{id}/delete") public String delete(@PathVariable Long id){ repo.delete(currentUserService.username(), id); return "redirect:/settings"; }
    @PostMapping("/settings/fillers/{id}/toggle") public String toggle(@PathVariable Long id,@RequestParam boolean enabled){ repo.toggle(currentUserService.username(), id, enabled); return "redirect:/settings"; }
}
