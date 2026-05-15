package com.example.audiotext.config;

import com.example.audiotext.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;import org.springframework.context.annotation.Configuration;

@Configuration
public class GigaChatConfig {
    private static final Logger log = LoggerFactory.getLogger(GigaChatConfig.class);

    @Bean
    public GigaChatService gigaChatService(AppProperties props){
        var giga = props.getGigachat();
        if(giga.isEnabled() && giga.getCredentials()!=null && !giga.getCredentials().isBlank()){
            log.info("AI-постобработка: используется RealGigaChatService");
            return new RealGigaChatService(giga);
        }
        log.info("AI-постобработка: используется fallback-сервис, real credentials не заданы или отключены");
        return new MockGigaChatService();
    }
}
