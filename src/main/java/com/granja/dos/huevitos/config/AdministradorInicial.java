package com.granja.dos.huevitos.config;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.granja.dos.huevitos.models.segurity.Rol;
import com.granja.dos.huevitos.models.segurity.Usuario;
import com.granja.dos.huevitos.repository.RolRepository;
import com.granja.dos.huevitos.repository.UsuarioRepository;

@Component
@ConditionalOnProperty(name = "app.bootstrap.enabled", havingValue = "true")
public class AdministradorInicial implements ApplicationRunner {
    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final PasswordEncoder encoder;
    private final String username;
    private final String password;

    public AdministradorInicial(UsuarioRepository usuarios, RolRepository roles, PasswordEncoder encoder,
            @Value("${app.bootstrap.username}") String username, @Value("${app.bootstrap.password}") String password) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.encoder = encoder;
        this.username = username;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (username.isBlank() || username.length() > 50 || password.isBlank()
                || password.length() < 12 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException("Configure un usuario de hasta 50 caracteres y una contraseña de 12 caracteres a 72 bytes para el administrador inicial.");
        }
        if (usuarios.count() > 0) {
            return;
        }
        Rol rol = roles.findByNombre("ADMIN").orElseGet(() -> {
            var nuevo = new Rol();
            nuevo.setNombre("ADMIN");
            nuevo.setDescripcion("Administrador del sistema");
            nuevo.setCreat(LocalDateTime.now());
            return roles.save(nuevo);
        });
        var usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(encoder.encode(password));
        usuario.setRol(rol);
        usuario.setEstado(true);
        usuario.setCreat(LocalDateTime.now());
        usuarios.save(usuario);
    }
}
