package com.example.audiotext.service;

import com.example.audiotext.model.PostProcessingResult;
import com.example.audiotext.model.WordInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class TextPostProcessingService {
    private final Set<String> fillers;
    private final Map<String, String> dictionary;

    public TextPostProcessingService() { this.fillers = loadFillers(); this.dictionary = loadDictionary(); }

    public PostProcessingResult process(String rawText, List<WordInfo> words) {
        PostProcessingResult r = new PostProcessingResult();
        if (rawText == null || rawText.isBlank()) { r.setProcessedText(""); return r; }

        // 1) Нормализация пробелов.
        String normalized = rawText.replaceAll("\\s+", " ").trim();

        // 2) Удаление повторов + 3) удаление слов-паразитов (однословных и многословных).
        List<String> tokens = new ArrayList<>(Arrays.asList(normalized.split(" ")));
        tokens = removeDuplicates(tokens, r);
        tokens = removeFillers(tokens, r);

        // 4-5) Безопасные замены терминов по границам слов.
        String text = String.join(" ", tokens);
        for (var e : dictionary.entrySet()) {
            String before = text;
            text = Pattern.compile("(?i)\\b" + Pattern.quote(e.getKey()) + "\\b").matcher(text).replaceAll(e.getValue());
            if (!before.equals(text)) r.getReplacements().put(e.getKey(), e.getValue());
        }

        // Локальный алгоритм выполняет черновую предобработку. Качественная пунктуация и абзацы выполняются на этапе AI-постобработки.
        text = applySoftPunctuation(text, words);
        text = normalizeSpacing(text);
        text = capitalizeFirstLetter(text);
        text = addFinalPunctuation(text);

        r.setProcessedText(text);
        return r;
    }

    private List<String> removeDuplicates(List<String> tokens, PostProcessingResult r) {
        List<String> out = new ArrayList<>();
        String prev = null;
        for (String token : tokens) {
            String t = token.trim();
            if (t.isBlank()) continue;
            if (prev != null && prev.equalsIgnoreCase(t)) { r.setRemovedDuplicatesCount(r.getRemovedDuplicatesCount() + 1); continue; }
            out.add(t); prev = t;
        }
        return out;
    }

    private List<String> removeFillers(List<String> tokens, PostProcessingResult r) {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            String one = tokens.get(i).toLowerCase(Locale.ROOT);
            String two = i + 1 < tokens.size() ? (one + " " + tokens.get(i + 1).toLowerCase(Locale.ROOT)) : "";
            String three = i + 2 < tokens.size()
                    ? (two + " " + tokens.get(i + 2).toLowerCase(Locale.ROOT))
                    : "";
            if (!three.isBlank() && fillers.contains(three)) { r.setRemovedFillerWordsCount(r.getRemovedFillerWordsCount() + 3); i += 2; continue; }
            if (!two.isBlank() && fillers.contains(two)) { r.setRemovedFillerWordsCount(r.getRemovedFillerWordsCount() + 2); i++; continue; }
            if (fillers.contains(one)) { r.setRemovedFillerWordsCount(r.getRemovedFillerWordsCount() + 1); continue; }
            out.add(tokens.get(i));
        }
        return out;
    }

    private String applySoftPunctuation(String text, List<WordInfo> words) {
        // Базовый режим — не вставлять точки внутри текста без надёжного выравнивания токенов.
        // Это защищает от артефактов вида "кто в. Не сидел...".
        return text;
    }

    private String normalizeSpacing(String text) {
        return text.replaceAll("\\s+([,.;:!?])", "$1").replaceAll("\\s+", " ").trim();
    }

    private String capitalizeFirstLetter(String text) {
        if (text.isBlank()) return text;
        char first = text.charAt(0);
        if (Character.isLetter(first)) return Character.toUpperCase(first) + text.substring(1);
        return text;
    }

    private String addFinalPunctuation(String text) {
        if (text.isBlank()) return text;
        if (text.endsWith(".") || text.endsWith("!") || text.endsWith("?")) return text;
        return text + ".";
    }

    private Set<String> loadFillers() { try { String s = new String(new ClassPathResource("config/filler_words_ru.txt").getInputStream().readAllBytes(), StandardCharsets.UTF_8); Set<String> set = new HashSet<>(); for (String line : s.split("\\R")) if (!line.isBlank()) set.add(line.trim().toLowerCase()); return set; } catch (Exception e) { return Set.of("ну", "эээ", "эм", "как бы"); } }
    private Map<String, String> loadDictionary() { try { String s = new String(new ClassPathResource("config/term_dictionary.json").getInputStream().readAllBytes(), StandardCharsets.UTF_8); return new ObjectMapper().readValue(s, new TypeReference<>() {}); } catch (Exception e) { return Map.of("джава", "Java", "воск", "Vosk"); } }
}
