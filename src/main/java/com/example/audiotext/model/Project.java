package com.example.audiotext.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Project {
    private Long id; private String title; private String originalFileName; private String originalFilePath; private String convertedFilePath;
    private ProjectStatus status = ProjectStatus.CREATED; private String errorMessage; private LocalDateTime createdAt; private LocalDateTime updatedAt;
    private Double durationSeconds = 0.0; private String rawText; private String processedText; private String aiText; private TextAnalysisResult analysisResult;
    private List<TranscriptionSegment> segments = new ArrayList<>();
    public Long getId(){return id;} public void setId(Long id){this.id=id;} public String getTitle(){return title;} public void setTitle(String title){this.title=title;}
    public String getOriginalFileName(){return originalFileName;} public void setOriginalFileName(String v){originalFileName=v;} public String getOriginalFilePath(){return originalFilePath;} public void setOriginalFilePath(String v){originalFilePath=v;}
    public String getConvertedFilePath(){return convertedFilePath;} public void setConvertedFilePath(String v){convertedFilePath=v;} public ProjectStatus getStatus(){return status;} public void setStatus(ProjectStatus s){status=s;}
    public String getErrorMessage(){return errorMessage;} public void setErrorMessage(String e){errorMessage=e;} public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime t){createdAt=t;}
    public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime t){updatedAt=t;} public Double getDurationSeconds(){return durationSeconds;} public void setDurationSeconds(Double d){durationSeconds=d;}
    public String getRawText(){return rawText;} public void setRawText(String t){rawText=t;} public String getProcessedText(){return processedText;} public void setProcessedText(String t){processedText=t;} public String getAiText(){return aiText;} public void setAiText(String t){aiText=t;}
    public TextAnalysisResult getAnalysisResult(){return analysisResult;} public void setAnalysisResult(TextAnalysisResult r){analysisResult=r;} public List<TranscriptionSegment> getSegments(){return segments;} public void setSegments(List<TranscriptionSegment> s){segments=s;}
}
