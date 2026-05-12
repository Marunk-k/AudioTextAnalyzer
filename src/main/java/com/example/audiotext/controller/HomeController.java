package com.example.audiotext.controller;
import org.springframework.stereotype.Controller;import org.springframework.web.bind.annotation.GetMapping;
@Controller public class HomeController { @GetMapping("/") public String index(){return "index";} @GetMapping("/upload") public String upload(){return "upload";} @GetMapping("/projects") public String projects(){return "projects";} @GetMapping("/settings") public String settings(){return "settings";} }
