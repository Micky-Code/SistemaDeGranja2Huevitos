package com.granja.dos.huevitos.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.granja.dos.huevitos.dto.RolResponse;
import com.granja.dos.huevitos.dto.UsuarioMantenimientoRequest;
import com.granja.dos.huevitos.dto.UsuarioMantenimientoResponse;
import com.granja.dos.huevitos.exception.BadRequestException;
import com.granja.dos.huevitos.exception.ExceptionResponse;
import com.granja.dos.huevitos.models.segurity.Usuario;
import com.granja.dos.huevitos.repository.RolRepository;
import com.granja.dos.huevitos.repository.UsuarioRepository;

@Service
public class UsuarioMantenimientoService {
    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final PasswordEncoder encoder;

    public UsuarioMantenimientoService(UsuarioRepository usuarios, RolRepository roles, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioMantenimientoResponse> listar() {
        return usuarios.findAllByOrderByIdUsuarioAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<RolResponse> listarRoles() {
        return roles.findAll().stream().map(rol -> new RolResponse(rol.getIdRol(), rol.getNombre())).toList();
    }

    @Transactional
    public UsuarioMantenimientoResponse crear(UsuarioMantenimientoRequest request) {
        var usuario = new Usuario();
        aplicar(usuario, request, true);
        usuario.setCreat(LocalDateTime.now());
        return guardar(usuario);
    }

    @Transactional
    public UsuarioMantenimientoResponse actualizar(Integer id, UsuarioMantenimientoRequest request) {
        var usuario = usuarios.findById(id)
                .orElseThrow(() -> new ExceptionResponse(404, "Usuario no encontrado."));
        aplicar(usuario, request, false);
        return guardar(usuario);
    }

    private void aplicar(Usuario usuario, UsuarioMantenimientoRequest request, boolean nuevo) {
        String username = request.username().trim();
        if (username.isEmpty()) {
            throw new BadRequestException("El nombre de usuario es obligatorio.");
        }
        usuarios.findByUsername(username).filter(existente -> !existente.getIdUsuario().equals(usuario.getIdUsuario()))
                .ifPresent(existente -> { throw new BadRequestException("El nombre de usuario ya existe."); });
        var rol = roles.findById(request.idRol())
                .orElseThrow(() -> new BadRequestException("Seleccione un rol válido."));
        usuario.setUsername(username);
        usuario.setRol(rol);
        usuario.setEstado(request.estado());
        String password = request.password();
        if (nuevo || (password != null && !password.isBlank())) {
            if (password == null || password.length() < 7
                    || password.getBytes(StandardCharsets.UTF_8).length > 72) {
                throw new BadRequestException("La contraseña debe tener al menos 7 caracteres.");
            }
            usuario.setPassword(encoder.encode(password));
        }
    }

    private UsuarioMantenimientoResponse guardar(Usuario usuario) {
        try {
            return toResponse(usuarios.saveAndFlush(usuario));
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("El nombre de usuario ya existe o los datos no son válidos.");
        }
    }

    private UsuarioMantenimientoResponse toResponse(Usuario usuario) {
        return new UsuarioMantenimientoResponse(usuario.getIdUsuario(), usuario.getUsername(),
                usuario.getRol().getIdRol(), usuario.getRol().getNombre(), usuario.getEstado());
    }
}
