package com.granja.dos.huevitos.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.granja.dos.huevitos.models.Rol;

public interface RolRepository extends JpaRepository<Rol, Integer> {
    Optional<Rol> findByNombre(String nombre);
}
