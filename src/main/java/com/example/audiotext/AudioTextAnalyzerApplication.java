package com.example.audiotext;

import com.example.audiotext.config.AppProperties;
import com.example.audiotext.service.StorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class AudioTextAnalyzerApplication {
    public static void main(String[] args) { SpringApplication.run(AudioTextAnalyzerApplication.class, args); }
    @Bean
    CommandLineRunner init(StorageService storageService) { return args -> storageService.ensureStorageDirectoriesExist(); }
}
