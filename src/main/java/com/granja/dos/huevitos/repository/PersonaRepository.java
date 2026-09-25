package com.granja.dos.huevitos.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.granja.dos.huevitos.models.personal.Persona;

public interface PersonaRepository extends JpaRepository<Persona, Integer> {
    Optional<Persona> findByCorreoIgnoreCase(String correo);
}
