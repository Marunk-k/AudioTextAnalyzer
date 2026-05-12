package com.example.audiotext.service;
import com.example.audiotext.model.*;import org.springframework.stereotype.Service;import java.util.*;
@Service
public class TextPostProcessingService {
    private static final Set<String> FILLERS=Set.of("ну","эээ","эм","типа","значит","короче");
    public PostProcessingResult process(String rawText, List<WordInfo> words){
        PostProcessingResult r=new PostProcessingResult(); if(rawText==null||rawText.isBlank()){r.setProcessedText(""); return r;}
        String[] toks=rawText.toLowerCase().replaceAll("\\s+"," ").trim().split(" "); List<String> out=new ArrayList<>(); String prev="";
        for(String t:toks){ if(t.equals(prev)){r.setRemovedDuplicatesCount(r.getRemovedDuplicatesCount()+1); continue;} if(FILLERS.contains(t)){r.setRemovedFillerWordsCount(r.getRemovedFillerWordsCount()+1); continue;} out.add(t); prev=t; }
        String text=String.join(" ",out).replace("джава","Java").replace("воск","Vosk").replace("спринг бут","Spring Boot");
        if(!text.endsWith(".")) text=text+"."; text=Character.toUpperCase(text.charAt(0))+text.substring(1); r.setProcessedText(text); return r;
    }
}
