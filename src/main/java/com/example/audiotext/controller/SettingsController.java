package com.example.audiotext.controller;

import com.example.audiotext.repository.UserDictionaryRepository;
import com.example.audiotext.service.CurrentUserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SettingsController {
    private final UserDictionaryRepository repo; private final CurrentUserService currentUserService;
    public SettingsController(UserDictionaryRepository repo, CurrentUserService currentUserService){this.repo=repo;this.currentUserService=currentUserService;}
    @GetMapping({"/settings", "/dictionaries"}) public String page(Model model){ String username=currentUserService.username(); model.addAttribute("entries", repo.findEntriesByUserLoginAndType(username, UserDictionaryRepository.FILLER_WORDS)); model.addAttribute("replacementEntries", repo.findEntriesByUserLoginAndType(username, UserDictionaryRepository.REPLACEMENTS)); return "settings"; }
    @PostMapping("/settings/fillers") public String add(@RequestParam String value){ if(value!=null&&!value.isBlank()) repo.add(currentUserService.username(), value); return "redirect:/settings"; }
    @PostMapping("/settings/replacements") public String addReplacement(@RequestParam String sourceValue, @RequestParam String targetValue, RedirectAttributes ra){ if(sourceValue==null||sourceValue.isBlank()||targetValue==null||targetValue.isBlank()) ra.addFlashAttribute("warning","Для замены заполните исходное и целевое значение."); else repo.add(currentUserService.username(), UserDictionaryRepository.REPLACEMENTS, sourceValue, targetValue); return "redirect:/settings"; }
    @PostMapping("/settings/dictionary/{id}/delete") public String deleteAny(@PathVariable Long id){ repo.delete(currentUserService.username(), id); return "redirect:/settings"; }
    @PostMapping("/settings/fillers/{id}/delete") public String delete(@PathVariable Long id){ return deleteAny(id); }
    @PostMapping("/settings/fillers/{id}/toggle") public String toggle(@PathVariable Long id,@RequestParam boolean enabled){ repo.toggle(currentUserService.username(), id, enabled); return "redirect:/settings"; }
}
