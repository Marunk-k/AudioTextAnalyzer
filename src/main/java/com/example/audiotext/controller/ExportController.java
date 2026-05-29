package com.example.audiotext.controller;

import com.example.audiotext.model.ExportFormat;
import com.example.audiotext.repository.ProjectRepository;
import com.example.audiotext.service.CurrentUserService;
import com.example.audiotext.service.ExportService;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;

@Controller
public class ExportController {
    private final ProjectRepository repo; private final CurrentUserService currentUserService; private final ExportService export;
    public ExportController(ProjectRepository repo, ExportService export, CurrentUserService currentUserService){this.repo=repo;this.export=export;this.currentUserService=currentUserService;}

    @GetMapping("/projects/{id}/export/txt") @ResponseBody public ResponseEntity<byte[]> txt(@PathVariable Long id){ return export(id, ExportFormat.TXT, "text/plain;charset=UTF-8"); }
    @GetMapping("/projects/{id}/export/json") @ResponseBody public ResponseEntity<byte[]> json(@PathVariable Long id){ return export(id, ExportFormat.JSON, MediaType.APPLICATION_JSON_VALUE); }
    @GetMapping("/projects/{id}/export/docx") @ResponseBody public ResponseEntity<byte[]> docx(@PathVariable Long id){ return export(id, ExportFormat.DOCX, "application/vnd.openxmlformats-officedocument.wordprocessingml.document"); }
    @GetMapping("/projects/{id}/export/pdf") @ResponseBody public ResponseEntity<byte[]> pdf(@PathVariable Long id){ return export(id, ExportFormat.PDF, MediaType.APPLICATION_PDF_VALUE); }
    private ResponseEntity<byte[]> export(Long id, ExportFormat format, String contentType){
        var p=repo.findByIdAndOwner(id, currentUserService.username()).orElseThrow();
        var existing=repo.findExportFile(id, format);
        if(existing.isPresent()) return file(existing.get().fileData(), existing.get().fileName(), existing.get().contentType());
        Path path = switch (format) { case TXT -> export.exportToTxt(p); case JSON -> export.exportToJson(p); case DOCX -> export.exportToDocx(p); case PDF -> export.exportToPdf(p); };
        try { byte[] data=Files.readAllBytes(path); String fileName="result."+format.name().toLowerCase(); repo.upsertExportFile(id, format, fileName, contentType, data); return file(data,fileName,contentType); } catch (Exception e) { throw new RuntimeException("Не удалось сохранить экспорт", e); }
    }
    private ResponseEntity<byte[]> file(byte[] data,String name,String contentType){ return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename="+name).contentType(MediaType.parseMediaType(contentType)).body(data); }
}
