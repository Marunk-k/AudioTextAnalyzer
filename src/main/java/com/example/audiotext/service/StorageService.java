package com.example.audiotext.service;
import com.example.audiotext.model.ExportFormat;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;
public interface StorageService {
  Path storeUploadedFile(MultipartFile file, String projectTitle);
  Path createProjectDirectory(Long projectId);
  Path getConvertedPath(Long projectId);
  Path getExportPath(Long projectId, ExportFormat format);
  void ensureStorageDirectoriesExist();
}
