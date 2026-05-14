package com.example.audiotext.model;
public class TranscriptionSegment { private double start,end,confidence; private String text;
 public TranscriptionSegment(){} public TranscriptionSegment(double s,double e,String t,double c){start=s;end=e;text=t;confidence=c;}
 public double getStart(){return start;} public void setStart(double v){start=v;} public double getEnd(){return end;} public void setEnd(double v){end=v;} public String getText(){return text;} public void setText(String v){text=v;} public double getConfidence(){return confidence;} public void setConfidence(double v){confidence=v;} }
