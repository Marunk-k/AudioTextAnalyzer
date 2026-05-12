package com.example.audiotext.model;
import java.util.*;
public class TranscriptionResult { private String rawText; private List<TranscriptionSegment> segments=new ArrayList<>(); private List<WordInfo> words=new ArrayList<>(); private double durationSeconds;
 public String getRawText(){return rawText;} public void setRawText(String v){rawText=v;} public List<TranscriptionSegment> getSegments(){return segments;} public void setSegments(List<TranscriptionSegment> v){segments=v;} public List<WordInfo> getWords(){return words;} public void setWords(List<WordInfo> v){words=v;} public double getDurationSeconds(){return durationSeconds;} public void setDurationSeconds(double v){durationSeconds=v;} }
