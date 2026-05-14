package com.example.audiotext.model;
import java.util.*;
public class TextAnalysisResult { public int wordCount,sentenceCount,paragraphCount,uniqueWordCount; public double averageSentenceLength,durationSeconds,wordsPerMinute; public Map<String,Integer> keywordFrequency=new LinkedHashMap<>(), fillerWordFrequency=new LinkedHashMap<>(); public List<WordInfo> lowConfidenceWords=new ArrayList<>(); public String algorithmicSummary;
}
