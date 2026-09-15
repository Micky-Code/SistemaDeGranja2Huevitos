package com.granja.dos.huevitos.controller;

import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.granja.dos.huevitos.dto.LoginRequest;
import com.granja.dos.huevitos.dto.UsuarioSesionResponse;
import com.granja.dos.huevitos.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final SecurityContextRepository contexts;
    private final SessionAuthenticationStrategy sessions;

    public AuthController(AuthService authService, SecurityContextRepository contexts,
            SessionAuthenticationStrategy sessions) {
        this.authService = authService;
        this.contexts = contexts;
        this.sessions = sessions;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("headerName", token.getHeaderName(), "token", token.getToken());
    }

    @PostMapping("/login")
    public UsuarioSesionResponse login(@Valid @RequestBody LoginRequest credentials,
            HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = authService.autenticar(credentials);
        var usuario = authService.usuarioActual(authentication.getName());
        sessions.onAuthentication(authentication, request, response);
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        contexts.saveContext(context, request, response);
        return usuario;
    }

    @GetMapping("/me")
    public UsuarioSesionResponse me(Authentication authentication) {
        return authService.usuarioActual(authentication.getName());
    }
}
