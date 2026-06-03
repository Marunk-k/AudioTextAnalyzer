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
    List<Project> findRecentByOwner(String login, int limit);
    boolean existsByOwnerAndTitle(String login, String title);
    boolean existsByOwnerAndTitleExcludingProject(String login, String title, Long projectId);
    void update(Project p);
    void updateStatus(Long id, ProjectStatus status);
    void updateAnalysis(Long projectId, TextAnalysisResult result);
    void saveSegments(Long projectId, List<TranscriptionSegment> segments);
    List<TranscriptionSegment> findSegmentsByProjectId(Long projectId);
    void saveAudioFile(Long projectId, String fileName, String contentType, byte[] fileData);
    Optional<AudioFile> findOriginalAudioFile(Long projectId);
    void upsertExportFile(Long projectId, ExportFormat format, String fileName, String contentType, byte[] fileData);
    Optional<ExportedFile> findExportFile(Long projectId, ExportFormat format);
    void deleteById(Long id);

    record AudioFile(String fileName, String contentType, byte[] fileData) {}
    record ExportedFile(String fileName, String contentType, byte[] fileData) {}
}
