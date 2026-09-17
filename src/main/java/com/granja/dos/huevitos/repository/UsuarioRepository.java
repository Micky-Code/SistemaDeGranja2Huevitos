package com.granja.dos.huevitos.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.granja.dos.huevitos.models.segurity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    @EntityGraph(attributePaths = "rol")
    Optional<Usuario> findByUsername(String username);
}
