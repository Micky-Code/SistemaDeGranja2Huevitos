package com.granja.dos.huevitos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.granja.dos.huevitos.models.almacen.RecepcionAlmacen;

public interface RecepcionAlmacenRepository extends JpaRepository<RecepcionAlmacen, Integer> {
    @EntityGraph(attributePaths = { "detalles", "detalles.clasificacion" })
    List<RecepcionAlmacen> findAllByOrderByFechaDesc();
}
