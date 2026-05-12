package com.example.audiotext.repository;

import com.example.audiotext.model.Project;
import com.example.audiotext.model.TranscriptionSegment;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository {
    Project save(Project p);
    Optional<Project> findById(Long id);
    List<Project> findAll();
    void update(Project p);
    void saveSegments(Long projectId, List<TranscriptionSegment> segments);
    List<TranscriptionSegment> findSegmentsByProjectId(Long projectId);
}
