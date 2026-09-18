package com.granja.dos.huevitos.service.impl;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.granja.dos.huevitos.repository.UsuarioRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UsuarioDetailsServiceImpl implements UserDetailsService {
    private final UsuarioRepository usuarios;

    public UsuarioDetailsServiceImpl(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
    	var usuario = usuarios.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Credenciales inválidas."));

        return User.withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(usuario.getRol().getIdRol() != null && usuario.getRol().getIdRol() == 1
                        && "ADMIN".equals(usuario.getRol().getNombre()) ? "ROLE_ADMIN" : "ROLE_USER")
                .disabled(!Boolean.TRUE.equals(usuario.getEstado()))
                .build();        
    }
}
