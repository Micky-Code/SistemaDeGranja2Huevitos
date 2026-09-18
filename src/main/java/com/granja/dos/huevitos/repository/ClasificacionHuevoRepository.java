package com.granja.dos.huevitos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.granja.dos.huevitos.models.almacen.ClasificacionHuevo;

public interface ClasificacionHuevoRepository extends JpaRepository<ClasificacionHuevo, Integer> {
    Optional<ClasificacionHuevo> findByNombre(String nombre);
}
