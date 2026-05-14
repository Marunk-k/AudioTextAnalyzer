package com.example.audiotext.service;

import com.example.audiotext.model.TextAnalysisResult;
import com.example.audiotext.model.TranscriptionResult;
import com.example.audiotext.model.WordInfo;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class TextAnalysisService {
  private final Set<String> stopWords = loadWords("config/stop_words_ru.txt", Set.of("и","в","на","что"));
  private final Set<String> fillerWords = loadWords("config/filler_words_ru.txt", Set.of("ну","эээ"));

  public TextAnalysisResult analyze(String text, TranscriptionResult tr){
    TextAnalysisResult r=new TextAnalysisResult();
    if(text==null) text="";
    String[] words=text.toLowerCase().replaceAll("[^а-яa-z0-9\\s]"," ").trim().split("\\s+");
    if(words.length==1&&words[0].isBlank()) words=new String[0];

    r.wordCount=words.length;
    r.sentenceCount=Math.max(1,text.split("[.!?]+").length);
    r.paragraphCount=Math.max(1,text.split("\\n\\s*\\n").length);
    r.uniqueWordCount=(int) Arrays.stream(words).filter(w->!w.isBlank()).distinct().count();
    r.averageSentenceLength=r.sentenceCount==0?0:(double)r.wordCount/r.sentenceCount;

    if(tr!=null){
      r.durationSeconds=tr.getDurationSeconds();
      if(r.durationSeconds>0) r.wordsPerMinute=r.wordCount/(r.durationSeconds/60d);
      r.lowConfidenceWords=tr.getWords().stream().filter(w->w.getConfidence()<0.6).toList();
    }

    Map<String,Integer> kw=new HashMap<>();
    Map<String,Integer> fillers=new HashMap<>();
    for(String w:words){
      if(w.isBlank()) continue;
      if(fillerWords.contains(w)) fillers.merge(w,1,Integer::sum);
      if(!stopWords.contains(w)) kw.merge(w,1,Integer::sum);
    }
    r.keywordFrequency=sortTop(kw,10);
    r.fillerWordFrequency=sortTop(fillers,10);

    r.algorithmicSummary = summarize(text);
    return r;
  }

  private String summarize(String text){
    String[] s=text.split("(?<=[.!?])\\s+");
    if(s.length<=3) return text;
    return s[0]+" "+s[Math.max(1,s.length/2)]+" "+s[s.length-1];
  }

  private Map<String,Integer> sortTop(Map<String,Integer> src,int limit){
    return src.entrySet().stream().sorted((a,b)->b.getValue()-a.getValue()).limit(limit)
            .collect(LinkedHashMap::new,(m,e)->m.put(e.getKey(),e.getValue()),Map::putAll);
  }

  private Set<String> loadWords(String path, Set<String> fallback){
    try{
      String s = new String(new ClassPathResource(path).getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      Set<String> out = new HashSet<>();
      for(String l:s.split("\\R")) if(!l.isBlank()) out.add(l.trim().toLowerCase());
      return out;
    }catch(Exception e){ return fallback; }
  }
}
