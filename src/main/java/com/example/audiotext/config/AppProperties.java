package com.example.audiotext.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String tempDir = "data/temp";
    private Audio audio = new Audio();
    private Vosk vosk = new Vosk();
    private Processing processing = new Processing();
    private Giga gigachat = new Giga();

    public String getTempDir() { return tempDir; }
    public void setTempDir(String tempDir) { this.tempDir = tempDir; }
    public Audio getAudio() { return audio; }
    public Vosk getVosk() { return vosk; }
    public Processing getProcessing() { return processing; }
    public Giga getGigachat() { return gigachat; }

    public static class Audio { public String ffmpegPath = "ffmpeg"; public List<String> allowedExtensions = new ArrayList<>(); public String getFfmpegPath(){return ffmpegPath;} public void setFfmpegPath(String v){ffmpegPath=v;} public List<String> getAllowedExtensions(){return allowedExtensions;} public void setAllowedExtensions(List<String> v){allowedExtensions=v;} }
    public static class Vosk { public boolean enabled=true; public String modelPath; public int sampleRate=16000; public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;} public String getModelPath(){return modelPath;} public void setModelPath(String v){modelPath=v;} public int getSampleRate(){return sampleRate;} public void setSampleRate(int v){sampleRate=v;} }
    public static class Processing { public boolean useMockTranscriptionIfVoskUnavailable=true; public double lowConfidenceThreshold=0.6; public boolean isUseMockTranscriptionIfVoskUnavailable(){return useMockTranscriptionIfVoskUnavailable;} public void setUseMockTranscriptionIfVoskUnavailable(boolean v){useMockTranscriptionIfVoskUnavailable=v;} public double getLowConfidenceThreshold(){return lowConfidenceThreshold;} public void setLowConfidenceThreshold(double v){lowConfidenceThreshold=v;} }
    public static class Giga { public boolean enabled=false; public String credentials="",scope="GIGACHAT_API_PERS",model="GigaChat"; public boolean verifySslCerts=false; public int timeoutSeconds=60; public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;} public String getCredentials(){return credentials;} public void setCredentials(String v){credentials=v;} public String getScope(){return scope;} public void setScope(String v){scope=v;} public String getModel(){return model;} public void setModel(String v){model=v;} public boolean isVerifySslCerts(){return verifySslCerts;} public void setVerifySslCerts(boolean v){verifySslCerts=v;} public int getTimeoutSeconds(){return timeoutSeconds;} public void setTimeoutSeconds(int v){timeoutSeconds=v;} }
}
