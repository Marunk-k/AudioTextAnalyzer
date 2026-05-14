package com.example.audiotext.service;

import com.example.audiotext.config.AppProperties;
import com.example.audiotext.model.TextAnalysisResult;
import com.example.audiotext.model.TranscriptionResult;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TextAnalysisService {
    private final Set<String> stopWords = loadWords("config/stop_words_ru.txt", Set.of("и", "в", "на", "что"));
    private final Set<String> fillerWords = loadWords("config/filler_words_ru.txt", Set.of("ну", "эээ", "как бы"));
    private final double lowConfidenceThreshold;

    public TextAnalysisService() { this.lowConfidenceThreshold = 0.6; }
    public TextAnalysisService(AppProperties properties) { this.lowConfidenceThreshold = properties.getProcessing().getLowConfidenceThreshold(); }

    public TextAnalysisResult analyze(String text, TranscriptionResult tr) {
        TextAnalysisResult r = new TextAnalysisResult();
        String safeText = text == null ? "" : text.trim();
        if (safeText.isBlank()) return r;

        String[] words = safeText.toLowerCase().replaceAll("[^а-яa-z0-9\\s-]", " ").trim().split("\\s+");
        r.wordCount = (int) Arrays.stream(words).filter(w -> !w.isBlank()).count();

        String[] sentences = safeText.split("(?<=[.!?])\\s+");
        r.sentenceCount = (int) Arrays.stream(sentences).filter(s -> !s.isBlank()).count();
        r.paragraphCount = (int) Arrays.stream(safeText.split("\\n\\s*\\n")).filter(p -> !p.isBlank()).count();
        r.uniqueWordCount = (int) Arrays.stream(words).filter(w -> !w.isBlank()).distinct().count();
        r.averageSentenceLength = r.sentenceCount == 0 ? 0 : (double) r.wordCount / r.sentenceCount;

        if (tr != null) {
            r.durationSeconds = tr.getDurationSeconds();
            if (r.durationSeconds > 0) r.wordsPerMinute = r.wordCount / (r.durationSeconds / 60d);
            if (tr.getWords() != null) r.lowConfidenceWords = tr.getWords().stream().filter(w -> w.getConfidence() < lowConfidenceThreshold).toList();
        }

        Map<String, Integer> keywords = new HashMap<>();
        Map<String, Integer> fillers = new HashMap<>();
        for (int i = 0; i < words.length; i++) {
            String w = words[i];
            if (w.length() <= 2) continue;
            if (fillerWords.contains(w)) fillers.merge(w, 1, Integer::sum);
            if (!stopWords.contains(w) && !fillerWords.contains(w)) keywords.merge(w, 1, Integer::sum);
            if (i < words.length - 1) {
                String bi = w + " " + words[i + 1];
                if (fillerWords.contains(bi)) fillers.merge(bi, 1, Integer::sum);
            }
        }
        r.keywordFrequency = sortTop(keywords, 15);
        r.fillerWordFrequency = sortTop(fillers, 15);
        r.algorithmicSummary = summarize(safeText);
        return r;
    }

    // Алгоритмическое резюме: частоты значимых слов + scoring предложений.
    private String summarize(String text) {
        String[] sentences = Arrays.stream(text.split("(?<=[.!?])\\s+")).filter(s -> !s.isBlank()).toArray(String[]::new);
        if (sentences.length <= 3) return text;

        Map<String, Integer> freq = new HashMap<>();
        for (String s : sentences) {
            for (String w : s.toLowerCase().replaceAll("[^а-яa-z0-9\\s-]", " ").split("\\s+")) {
                if (w.length() <= 2 || stopWords.contains(w) || fillerWords.contains(w)) continue;
                freq.merge(w, 1, Integer::sum);
            }
        }

        Map<Integer, Integer> sentenceScores = new HashMap<>();
        for (int i = 0; i < sentences.length; i++) {
            int score = 0;
            for (String w : sentences[i].toLowerCase().replaceAll("[^а-яa-z0-9\\s-]", " ").split("\\s+")) {
                score += freq.getOrDefault(w, 0);
            }
            sentenceScores.put(i, score);
        }

        return sentenceScores.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(3)
                .map(Map.Entry::getKey)
                .sorted()
                .map(i -> sentences[i].trim())
                .collect(Collectors.joining(" "));
    }

    private Map<String, Integer> sortTop(Map<String, Integer> src, int limit) {
        return src.entrySet().stream().sorted((a, b) -> b.getValue() - a.getValue()).limit(limit)
                .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);
    }

    private Set<String> loadWords(String path, Set<String> fallback) {
        try { String s = new String(new ClassPathResource(path).getInputStream().readAllBytes(), StandardCharsets.UTF_8); Set<String> out = new HashSet<>(); for (String l : s.split("\\R")) if (!l.isBlank()) out.add(l.trim().toLowerCase()); return out; } catch (Exception e) { return fallback; }
    }
}
