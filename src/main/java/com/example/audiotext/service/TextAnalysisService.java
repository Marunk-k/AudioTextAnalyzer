package com.example.audiotext.service;
import com.example.audiotext.model.*;import org.springframework.stereotype.Service;import java.util.*;
@Service
public class TextAnalysisService {
  private static final Set<String> STOP=Set.of("и","в","на","с","по","что","как","это");
  public TextAnalysisResult analyze(String text, TranscriptionResult tr){ TextAnalysisResult r=new TextAnalysisResult(); if(text==null) text="";
    String[] words=text.toLowerCase().replaceAll("[^а-яa-z0-9\\s]"," ").trim().split("\\s+"); if(words.length==1&&words[0].isBlank()) words=new String[0];
    r.wordCount=words.length; r.sentenceCount=Math.max(1,text.split("[.!?]+").length); r.paragraphCount=Math.max(1,text.split("\\n\\s*\\n").length);
    r.uniqueWordCount=(int)Arrays.stream(words).filter(w->!w.isBlank()).distinct().count(); r.averageSentenceLength=r.sentenceCount==0?0:(double)r.wordCount/r.sentenceCount;
    if(tr!=null){ r.durationSeconds=tr.getDurationSeconds(); if(r.durationSeconds>0) r.wordsPerMinute=r.wordCount/(r.durationSeconds/60d); r.lowConfidenceWords=tr.getWords().stream().filter(w->w.getConfidence()<0.6).toList();}
    Map<String,Integer> f=new HashMap<>(); for(String w:words){ if(STOP.contains(w)||w.isBlank()) continue; f.merge(w,1,Integer::sum);} r.keywordFrequency=f.entrySet().stream().sorted((a,b)->b.getValue()-a.getValue()).limit(10).collect(LinkedHashMap::new,(m,e)->m.put(e.getKey(),e.getValue()),Map::putAll);
    r.algorithmicSummary=text.length()>300?text.substring(0,300)+"...":text; return r; }
}
