package com.example.audiotext.service;

import com.example.audiotext.model.Project;
import org.springframework.stereotype.Service;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

@Service
public class ExportService {
    private final StorageService storage;
    public ExportService(StorageService storage){this.storage=storage;}
    public Path exportToTxt(Project p){
        try{ Path out=storage.getExportPath(p.getId(), com.example.audiotext.model.ExportFormat.TXT);
            String body="Проект: "+p.getTitle()+"\nФайл: "+p.getOriginalFileName()+"\n\n"+(p.getProcessedText()==null?"":p.getProcessedText());
            Files.writeString(out, body, StandardCharsets.UTF_8); return out; }catch(Exception e){throw new RuntimeException("Ошибка экспорта TXT",e);} }
    public Path exportToJson(Project p){
        try{ Path out=storage.getExportPath(p.getId(), com.example.audiotext.model.ExportFormat.JSON);
            String json="{\n  \"id\": "+p.getId()+",\n  \"title\": \""+p.getTitle()+"\",\n  \"status\": \""+p.getStatus()+"\"\n}";
            Files.writeString(out, json, StandardCharsets.UTF_8); return out;}catch(Exception e){throw new RuntimeException("Ошибка экспорта JSON",e);} }
}
