package com.example.audiotext.controller;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    private final JdbcUserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;

    public AuthController(org.springframework.security.core.userdetails.UserDetailsService uds, PasswordEncoder passwordEncoder) {
        this.userDetailsManager = (JdbcUserDetailsManager) uds;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String passwordRepeat,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (!StringUtils.hasText(username)) {
            model.addAttribute("error", "Логин не может быть пустым.");
            return "register";
        }
        if (!StringUtils.hasText(password)) {
            model.addAttribute("error", "Пароль не может быть пустым.");
            return "register";
        }
        if (!password.equals(passwordRepeat)) {
            model.addAttribute("error", "Пароли не совпадают.");
            return "register";
        }
        if (userDetailsManager.userExists(username)) {
            model.addAttribute("error", "Пользователь с таким логином уже существует.");
            return "register";
        }

        userDetailsManager.createUser(User.withUsername(username)
                .password(passwordEncoder.encode(password))
                .roles("USER")
                .build());

        redirectAttributes.addFlashAttribute("success", "Регистрация выполнена. Теперь войдите в систему.");
        return "redirect:/login";
    }
}
