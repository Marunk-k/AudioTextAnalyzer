package com.example.audiotext.repository;

import com.example.audiotext.model.*;
import java.util.List;
import java.util.Optional;

public interface ProjectRepository {
    Project save(Project p);
    Optional<Project> findById(Long id);
    Optional<Project> findByIdAndOwner(Long id, String login);
    List<Project> findAll();
    List<Project> findAllByOwner(String login);
    void update(Project p);
    void updateStatus(Long id, ProjectStatus status);
    void updateAnalysis(Long projectId, TextAnalysisResult result);
    void saveSegments(Long projectId, List<TranscriptionSegment> segments);
    List<TranscriptionSegment> findSegmentsByProjectId(Long projectId);
    void deleteById(Long id);
}
