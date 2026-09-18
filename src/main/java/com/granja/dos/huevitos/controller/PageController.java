package com.granja.dos.huevitos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/menu")
    public String menu(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        return "menu";
    }

    @GetMapping("/almacen")
    public String almacen(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        return "almacen";
    }

    @GetMapping("/reportes")
    public String reportes(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        return "reportes";
    }
}
