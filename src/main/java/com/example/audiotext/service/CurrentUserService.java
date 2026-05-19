package com.example.audiotext.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    public String username() { return SecurityContextHolder.getContext().getAuthentication().getName(); }
}
