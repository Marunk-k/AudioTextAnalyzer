package com.example.audiotext.service;
import org.springframework.stereotype.Service;
@Service
public class MockGigaChatService implements GigaChatService {
    public String improveText(String text){ return "Демонстрационный AI-режим. GigaChat API не настроен.\n\n" + (text==null?"":text.trim()); }
    public String summarizeText(String text){ if(text==null||text.isBlank()) return ""; String t=text.trim(); return t.length()>220?t.substring(0,220)+"...":t; }
}
