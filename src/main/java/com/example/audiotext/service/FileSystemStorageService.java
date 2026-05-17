package com.example.audiotext.service;
import com.example.audiotext.config.AppProperties;
import com.example.audiotext.model.ExportFormat;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;import java.nio.file.*;import java.util.UUID;
@Service
public class FileSystemStorageService implements StorageService {
 private final AppProperties p; public FileSystemStorageService(AppProperties p){this.p=p;}
 public Path storeUploadedFile(MultipartFile file, String title){ try{ String n=UUID.randomUUID()+"_"+file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]","_"); Path dest=Path.of(p.getTempDir(),"uploads").resolve(n); Files.createDirectories(dest.getParent()); file.transferTo(dest); return dest;}catch(Exception e){throw new RuntimeException(e);} }
 public Path createProjectDirectory(Long id){ try{ Path d=Path.of(p.getTempDir(),"converted","project_"+id); Files.createDirectories(d); return d;}catch(IOException e){throw new RuntimeException(e);} }
 public Path getConvertedPath(Long id){ return Path.of(p.getTempDir(),"converted","project_"+id,"audio.wav"); }
 public Path getExportPath(Long id, ExportFormat f){ Path d=Path.of(p.getTempDir(),"exports","project_"+id); try{Files.createDirectories(d);}catch(IOException e){throw new RuntimeException(e);} return d.resolve("result."+f.name().toLowerCase()); }
 public void ensureStorageDirectoriesExist(){ try{Files.createDirectories(Path.of(p.getTempDir()));}catch(IOException e){throw new RuntimeException(e);} }
}
