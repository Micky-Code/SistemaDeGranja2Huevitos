package com.granja.dos.huevitos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.granja.dos.huevitos.models.personal.Guardia;

public interface GuardiaRepository extends JpaRepository<Guardia, Integer> {
    Optional<Guardia> findByEmpleado_IdEmpleado(Integer idEmpleado);
}
