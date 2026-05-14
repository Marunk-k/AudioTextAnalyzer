package com.example.audiotext.service;

import com.example.audiotext.model.PostProcessingResult;
import com.example.audiotext.model.WordInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class TextPostProcessingService {
    private final Set<String> fillers;
    private final Map<String, String> dictionary;

    public TextPostProcessingService() {
        this.fillers = loadFillers();
        this.dictionary = loadDictionary();
    }

    public PostProcessingResult process(String rawText, List<WordInfo> words) {
        PostProcessingResult r = new PostProcessingResult();
        if (rawText == null || rawText.isBlank()) {
            r.setProcessedText("");
            return r;
        }

        String cleaned = rawText.toLowerCase().replaceAll("\\s+", " ").trim();
        List<String> out = new ArrayList<>();
        String prev = "";
        for (String t : cleaned.split(" ")) {
            if (t.equals(prev)) { r.setRemovedDuplicatesCount(r.getRemovedDuplicatesCount() + 1); continue; }
            if (fillers.contains(t)) { r.setRemovedFillerWordsCount(r.getRemovedFillerWordsCount() + 1); continue; }
            out.add(t); prev = t;
        }

        String text = String.join(" ", out);
        for (var e : dictionary.entrySet()) {
            String before = text;
            text = text.replace(e.getKey(), e.getValue());
            if (!before.equals(text)) r.getReplacements().put(e.getKey(), e.getValue());
        }

        text = text.replaceAll("\\s+([,.;:!?])", "$1");
        if (!text.endsWith(".")) text += ".";
        text = Character.toUpperCase(text.charAt(0)) + text.substring(1);
        r.setProcessedText(text);
        return r;
    }

    private Set<String> loadFillers() {
        try {
            String s = new String(new ClassPathResource("config/filler_words_ru.txt").getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            Set<String> set = new HashSet<>();
            for (String line : s.split("\\R")) if (!line.isBlank()) set.add(line.trim().toLowerCase());
            return set;
        } catch (Exception e) { return Set.of("ну", "эээ", "эм"); }
    }

    private Map<String, String> loadDictionary() {
        try {
            String s = new String(new ClassPathResource("config/term_dictionary.json").getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return new ObjectMapper().readValue(s, new TypeReference<>() {});
        } catch (Exception e) { return Map.of("джава", "Java", "воск", "Vosk"); }
    }
}
