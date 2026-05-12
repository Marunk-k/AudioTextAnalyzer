package com.example.audiotext.model;
public class WordInfo { private String word; private double start,end,confidence;
 public WordInfo(){} public WordInfo(String w,double s,double e,double c){word=w;start=s;end=e;confidence=c;} public String getWord(){return word;} public double getStart(){return start;} public double getEnd(){return end;} public double getConfidence(){return confidence;} }
