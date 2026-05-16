package com.example.audiotext.controller;

import com.example.audiotext.service.DictionaryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DictionaryController {
    private final DictionaryService dictionaries;

    public DictionaryController(DictionaryService dictionaries) {
        this.dictionaries = dictionaries;
    }

    @GetMapping({"/settings", "/dictionaries"})
    public String settings(Model model) {
        String username = dictionaries.currentUsername();
        model.addAttribute("username", username);
        model.addAttribute("userDictionary", dictionaries.getUserDictionary(username));
        model.addAttribute("systemDictionary", dictionaries.getSystemDictionary());
        return "settings";
    }

    @PostMapping("/settings/dictionary/save")
    public String save(@RequestParam String source, @RequestParam String replacement) {
        String username = dictionaries.currentUsername();
        if (username != null && source != null && !source.isBlank() && replacement != null && !replacement.isBlank()) {
            dictionaries.saveUserTerm(username.trim(), source.trim(), replacement.trim());
        }
        return "redirect:/settings";
    }

    @PostMapping("/settings/dictionary/delete")
    public String delete(@RequestParam String source) {
        String username = dictionaries.currentUsername();
        if (username != null && source != null && !source.isBlank()) dictionaries.deleteUserTerm(username, source.trim());
        return "redirect:/settings";
    }
}
