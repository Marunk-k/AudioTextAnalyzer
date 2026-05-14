package com.example.audiotext.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public String handle(Exception ex, Model model){ model.addAttribute("error", ex.getMessage()); return "error"; }
}
