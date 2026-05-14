package com.example.audiotext.repository;

import com.example.audiotext.model.*;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository {
    Project save(Project p);
    Optional<Project> findById(Long id);
    List<Project> findAll();
    void update(Project p);
    void updateStatus(Long id, ProjectStatus status);
    void updateTexts(Long id, String rawText, String processedText, String aiText);
    void updateAnalysis(Long id, TextAnalysisResult result);
    void saveSegments(Long projectId, List<TranscriptionSegment> segments);
    List<TranscriptionSegment> findSegmentsByProjectId(Long projectId);
    void deleteById(Long id);
}
