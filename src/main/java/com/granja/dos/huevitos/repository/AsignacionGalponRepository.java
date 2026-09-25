package com.granja.dos.huevitos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.granja.dos.huevitos.models.personal.AsignacionGalpon;

public interface AsignacionGalponRepository extends JpaRepository<AsignacionGalpon, Integer> {
    @EntityGraph(attributePaths = "galpon")
    List<AsignacionGalpon> findAllByEmpleado_IdEmpleadoOrderByFechaInicioDesc(Integer idEmpleado);
}
