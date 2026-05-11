package com.example.audiotext.controller;

import com.example.audiotext.repository.ProjectRepository;
import com.example.audiotext.service.ExportService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class ExportController {
    private final ProjectRepository repo; private final ExportService export;
    public ExportController(ProjectRepository repo, ExportService export){this.repo=repo;this.export=export;}
    @GetMapping("/projects/{id}/export/txt") @ResponseBody
    public ResponseEntity<FileSystemResource> txt(@PathVariable Long id){ var p=repo.findById(id).orElseThrow(); var f=export.exportToTxt(p); return file(f,"result.txt"); }
    @GetMapping("/projects/{id}/export/json") @ResponseBody
    public ResponseEntity<FileSystemResource> json(@PathVariable Long id){ var p=repo.findById(id).orElseThrow(); var f=export.exportToJson(p); return file(f,"result.json"); }
    private ResponseEntity<FileSystemResource> file(java.nio.file.Path p,String name){ var r=new FileSystemResource(p); return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename="+name).contentType(MediaType.APPLICATION_OCTET_STREAM).body(r); }
}
