package com.example.audiotext.service; import com.example.audiotext.model.TranscriptionResult; import java.nio.file.Path;
public interface TranscriptionService { TranscriptionResult transcribe(Path wavFile); }
