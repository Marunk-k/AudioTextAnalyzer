package com.example.audiotext.controller;
import com.example.audiotext.model.*;import com.example.audiotext.repository.ProjectRepository;import com.example.audiotext.service.StorageService;import org.springframework.stereotype.Controller;import org.springframework.ui.Model;import org.springframework.web.bind.annotation.*;import org.springframework.web.multipart.MultipartFile;
@Controller
public class UploadController {
 private final StorageService storage; private final ProjectRepository repo; public UploadController(StorageService s, ProjectRepository r){storage=s;repo=r;}
 @PostMapping("/upload") public String doUpload(@RequestParam String title,@RequestParam MultipartFile file, Model model){ if(file.isEmpty()){model.addAttribute("error","Файл не выбран"); return "upload";} var path=storage.storeUploadedFile(file,title); Project p=new Project(); p.setTitle(title); p.setOriginalFileName(file.getOriginalFilename()); p.setOriginalFilePath(path.toString()); p.setStatus(ProjectStatus.UPLOADED); p=repo.save(p); return "redirect:/projects/"+p.getId(); }
}
