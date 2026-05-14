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
    private static final Set<String> PARAGRAPH_MARKERS = Set.of("во-первых", "во-вторых", "далее", "таким образом", "итак", "следовательно", "в заключение");
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

        // 6) Базовая пунктуация.
        text = applyPunctuation(text, words);

        // 7) Капитализация.
        text = capitalizeSentences(text);

        // 8) Разбиение на абзацы.
        text = splitParagraphs(text);

        r.setProcessedText(text.trim());
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
            if (!two.isBlank() && fillers.contains(two)) { r.setRemovedFillerWordsCount(r.getRemovedFillerWordsCount() + 2); i++; continue; }
            if (fillers.contains(one)) { r.setRemovedFillerWordsCount(r.getRemovedFillerWordsCount() + 1); continue; }
            out.add(tokens.get(i));
        }
        return out;
    }

    private String applyPunctuation(String text, List<WordInfo> words) {
        if (words == null || words.isEmpty()) {
            String[] t = text.split(" ");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < t.length; i++) {
                sb.append(t[i]);
                if ((i + 1) % 16 == 0) sb.append(". "); else sb.append(" ");
            }
            return sb.toString().trim();
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.size(); i++) {
            WordInfo w = words.get(i);
            sb.append(w.getWord()).append(" ");
            if (i < words.size() - 1) {
                double pause = words.get(i + 1).getStart() - w.getEnd();
                if (pause > 0.8) sb.append(". ");
                else if (pause >= 0.4) sb.append(", ");
            }
        }
        if (!sb.toString().trim().endsWith(".")) sb.append(".");
        return sb.toString().replaceAll("\\s+([,.;:!?])", "$1").replaceAll("\\s+", " ").trim();
    }

    private String capitalizeSentences(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        boolean cap = true;
        for (char c : text.toCharArray()) {
            if (cap && Character.isLetter(c)) { sb.append(Character.toUpperCase(c)); cap = false; }
            else sb.append(c);
            if (c == '.' || c == '!' || c == '?') cap = true;
        }
        return sb.toString();
    }

    private String splitParagraphs(String text) {
        String[] sentences = text.split("(?<=[.!?])\\s+");
        List<String> parts = new ArrayList<>();
        StringBuilder p = new StringBuilder();
        int count = 0;
        for (String s : sentences) {
            String low = s.trim().toLowerCase(Locale.ROOT);
            if (count >= 4 || PARAGRAPH_MARKERS.stream().anyMatch(low::startsWith)) {
                parts.add(p.toString().trim());
                p = new StringBuilder();
                count = 0;
            }
            p.append(s).append(" ");
            count++;
        }
        if (!p.isEmpty()) parts.add(p.toString().trim());
        return String.join("\n\n", parts);
    }

    private Set<String> loadFillers() { try { String s = new String(new ClassPathResource("config/filler_words_ru.txt").getInputStream().readAllBytes(), StandardCharsets.UTF_8); Set<String> set = new HashSet<>(); for (String line : s.split("\\R")) if (!line.isBlank()) set.add(line.trim().toLowerCase()); return set; } catch (Exception e) { return Set.of("ну", "эээ", "эм", "как бы"); } }
    private Map<String, String> loadDictionary() { try { String s = new String(new ClassPathResource("config/term_dictionary.json").getInputStream().readAllBytes(), StandardCharsets.UTF_8); return new ObjectMapper().readValue(s, new TypeReference<>() {}); } catch (Exception e) { return Map.of("джава", "Java", "воск", "Vosk"); } }
}
