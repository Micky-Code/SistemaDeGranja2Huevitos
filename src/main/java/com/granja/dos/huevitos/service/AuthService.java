package com.granja.dos.huevitos.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.granja.dos.huevitos.dto.LoginRequest;
import com.granja.dos.huevitos.dto.UsuarioSesionResponse;
import com.granja.dos.huevitos.repository.UsuarioRepository;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarios;

    public AuthService(AuthenticationManager authenticationManager, UsuarioRepository usuarios) {
        this.authenticationManager = authenticationManager;
        this.usuarios = usuarios;
    }

    public Authentication autenticar(LoginRequest request) {
        if (request.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new BadCredentialsException("Credenciales inválidas.");
        }
        return authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
    }

    @Transactional(readOnly = true)
    public UsuarioSesionResponse usuarioActual(String username) {
        var usuario = usuarios.findByUsername(username)
                .filter(u -> Boolean.TRUE.equals(u.getEstado()))
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas."));
        return new UsuarioSesionResponse(usuario.getIdUsuario(), usuario.getUsername(), usuario.getRol().getNombre());
    }
}
