package com.example.audiotext.config;

import com.example.audiotext.service.GigaChatService;
import com.example.audiotext.service.MockGigaChatService;
import com.example.audiotext.service.RealGigaChatService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GigaChatConfigTest {
    @Test
    void fallbackCreatedWhenDisabled(){
        AppProperties props=new AppProperties();
        props.getGigachat().setEnabled(false);
        GigaChatService service=new GigaChatConfig().gigaChatService(props);
        assertInstanceOf(MockGigaChatService.class, service);
    }

    @Test
    void startupSafeWhenCredentialsEmpty(){
        AppProperties props=new AppProperties();
        props.getGigachat().setEnabled(true);
        props.getGigachat().setCredentials(" ");
        assertDoesNotThrow(() -> new GigaChatConfig().gigaChatService(props));
    }

    @Test
    void realCreatedWhenEnabledAndCredentialsPresent(){
        AppProperties props=new AppProperties();
        props.getGigachat().setEnabled(true);
        props.getGigachat().setCredentials("base64creds");
        GigaChatService service=new GigaChatConfig().gigaChatService(props);
        assertInstanceOf(RealGigaChatService.class, service);
    }
}
