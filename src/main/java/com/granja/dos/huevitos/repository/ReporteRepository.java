package com.granja.dos.huevitos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.granja.dos.huevitos.models.reportes.Reporte;

public interface ReporteRepository extends JpaRepository<Reporte, Integer> {
    @EntityGraph(attributePaths = { "detalles", "usuario" })
    List<Reporte> findAllByOrderByFechaGeneracionDesc();
}
