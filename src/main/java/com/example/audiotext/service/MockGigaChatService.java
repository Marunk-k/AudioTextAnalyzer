package com.example.audiotext.service;

public class MockGigaChatService implements GigaChatService {
    @Override
    public String improveText(String text){
        if(text==null||text.isBlank()) return "";
        return text.trim();
    }

    @Override
    public String summarizeText(String text){
        if(text==null||text.isBlank()) return "";
        String t=text.trim();
        return t.length()>220?t.substring(0,220)+"...":t;
    }
}
