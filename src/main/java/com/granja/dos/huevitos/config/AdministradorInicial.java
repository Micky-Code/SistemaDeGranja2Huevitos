package com.granja.dos.huevitos.config;

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
@ConditionalOnProperty(name = "app.bootstrap.enabled", havingValue = "true", matchIfMissing = true)
public class AdministradorInicial implements ApplicationRunner {
    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final PasswordEncoder encoder;
    private final String username;
    private final String password;

    public AdministradorInicial(UsuarioRepository usuarios, RolRepository roles, PasswordEncoder encoder,
            @Value("${app.bootstrap.username:admin}") String username, 
            @Value("${app.bootstrap.password:admin123}") String password) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.encoder = encoder;
        this.username = username;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Rol rol = roles.findByNombre("ADMIN").orElseGet(() -> {
            var nuevo = new Rol();
            nuevo.setNombre("ADMIN");
            nuevo.setDescripcion("Administrador del sistema");
            nuevo.setCreat(LocalDateTime.now());
            return roles.save(nuevo);
        });

        // Asegurar cuenta principal configurada (por defecto: admin / admin123)
        var optUser = usuarios.findByUsername(username);
        if (optUser.isEmpty()) {
            var usuario = new Usuario();
            usuario.setUsername(username);
            usuario.setPassword(encoder.encode(password));
            usuario.setRol(rol);
            usuario.setEstado(true);
            usuario.setCreat(LocalDateTime.now());
            usuarios.save(usuario);
        } else {
            Usuario u = optUser.get();
            u.setPassword(encoder.encode(password));
            u.setEstado(true);
            u.setRol(rol);
            usuarios.save(u);
        }

        // También asegurar que admin01 tenga admin123 para fácil acceso
        var optAdmin01 = usuarios.findByUsername("admin01");
        if (optAdmin01.isPresent()) {
            Usuario u = optAdmin01.get();
            u.setPassword(encoder.encode("admin123"));
            u.setEstado(true);
            u.setRol(rol);
            usuarios.save(u);
        }
    }
}
