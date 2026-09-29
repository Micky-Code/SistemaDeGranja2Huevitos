package com.granja.dos.huevitos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.granja.dos.huevitos.models.personal.Asignacion;

public interface AsignacionGalponRepository extends JpaRepository<Asignacion, Integer> {
    @EntityGraph(attributePaths = "galpon")
    List<Asignacion> findAllByEmpleado_IdEmpleadoOrderByFechaInicioDesc(Integer idEmpleado);
}
