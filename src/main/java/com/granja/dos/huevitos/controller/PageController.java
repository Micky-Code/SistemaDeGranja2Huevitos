package com.granja.dos.huevitos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;

@Controller
public class PageController {

    private void agregarUsuarioAlModelo(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        } else {
            model.addAttribute("username", "Invitado");
        }
    }

    @GetMapping({"/", "/login"})
    public String login() {
        return "login";
    }

    @GetMapping("/menu")

    public String menu(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("isAdmin", authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())));
        return "menu";
    }

    @GetMapping("/usuarios")
    public String usuarios(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        return "usuarios";
=======
    public String menu(Model model, Principal principal) {
        agregarUsuarioAlModelo(model, principal);
        return "menu";
    }

    @GetMapping("/infraestructura")
    public String infraestructura(Model model, Principal principal) {
        agregarUsuarioAlModelo(model, principal);
        return "infraestructura";
    }

    @GetMapping("/produccion")
    public String produccion(Model model, Principal principal) {
        agregarUsuarioAlModelo(model, principal);
        return "produccion";

    }
}
