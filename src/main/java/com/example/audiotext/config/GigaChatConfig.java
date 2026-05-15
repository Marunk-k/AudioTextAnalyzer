package com.example.audiotext.config;

import com.example.audiotext.service.*;
import org.springframework.context.annotation.Bean;import org.springframework.context.annotation.Configuration;

@Configuration
public class GigaChatConfig {
    @Bean
    public GigaChatService gigaChatService(AppProperties props){
        var giga = props.getGigachat();
        if(giga.isEnabled() && giga.getCredentials()!=null && !giga.getCredentials().isBlank()){
            return new RealGigaChatService(giga);
        }
        return new MockGigaChatService();
    }
}
