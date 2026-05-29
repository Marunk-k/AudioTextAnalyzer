package com.example.audiotext.controller;

import com.example.audiotext.repository.UserDictionaryRepository;
import com.example.audiotext.service.CurrentUserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DictionaryController {
    private final UserDictionaryRepository dictionaries;
    private final CurrentUserService currentUserService;

    public DictionaryController(UserDictionaryRepository dictionaries, CurrentUserService currentUserService) {
        this.dictionaries = dictionaries;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/legacy-dictionaries")
    public String settings(Model model) {
        String username = currentUserService.username();
        model.addAttribute("entries", dictionaries.findEntriesByUserLoginAndType(username, UserDictionaryRepository.FILLER_WORDS));
        model.addAttribute("replacementEntries", dictionaries.findEntriesByUserLoginAndType(username, UserDictionaryRepository.REPLACEMENTS));
        return "settings";
    }

    @PostMapping("/settings/dictionary/save")
    public String save(@RequestParam String source, @RequestParam String replacement) {
        String username = currentUserService.username();
        if (username != null && source != null && !source.isBlank() && replacement != null && !replacement.isBlank()) {
            dictionaries.add(username, UserDictionaryRepository.REPLACEMENTS, source.trim(), replacement.trim());
        }
        return "redirect:/settings";
    }

    @PostMapping("/settings/dictionary/delete")
    public String delete(@RequestParam String source) {
        String username = currentUserService.username();
        if (username != null && source != null && !source.isBlank()) {
            dictionaries.findEntriesByUserLoginAndType(username, UserDictionaryRepository.REPLACEMENTS).stream()
                    .filter(e -> source.trim().equalsIgnoreCase(e.sourceValue()))
                    .findFirst()
                    .ifPresent(e -> dictionaries.delete(username, e.id()));
        }
        return "redirect:/settings";
    }
}
