package com.example.audiotext.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
public class AuthController {
    private final JdbcTemplate jdbc; private final PasswordEncoder passwordEncoder;
    public AuthController(JdbcTemplate jdbc, PasswordEncoder passwordEncoder){this.jdbc=jdbc;this.passwordEncoder=passwordEncoder;}
    @GetMapping("/login") public String loginPage(){return "login";} @GetMapping("/register") public String registerPage(){return "register";}
    @PostMapping("/register") public String register(@RequestParam String username,@RequestParam String password,@RequestParam String passwordRepeat,RedirectAttributes redirectAttributes,Model model){
        if (!StringUtils.hasText(username)||!StringUtils.hasText(password)){model.addAttribute("error","Логин и пароль обязательны.");return "register";}
        if (!password.equals(passwordRepeat)){model.addAttribute("error","Пароли не совпадают.");return "register";}
        Integer c = jdbc.queryForObject("select count(*) from users where login=?", Integer.class, username);
        if (c!=null && c>0){model.addAttribute("error","Пользователь с таким логином уже существует.");return "register";}
        jdbc.update("insert into users(login,password_hash,created_at) values(?,?,?)",username,passwordEncoder.encode(password), LocalDateTime.now());
        redirectAttributes.addFlashAttribute("success", "Регистрация выполнена. Теперь войдите в систему."); return "redirect:/login";
    }
}
