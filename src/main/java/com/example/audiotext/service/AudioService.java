package com.example.audiotext.service;

import java.nio.file.Path;

public interface AudioService {
    boolean isSupported(Path file);
    Path convertToWav(Path inputFile, Long projectId);
    double getDurationSeconds(Path file);
}
