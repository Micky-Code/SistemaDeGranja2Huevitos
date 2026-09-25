package com.granja.dos.huevitos.repository;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import com.granja.dos.huevitos.models.personal.Empleado;

public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {
    @EntityGraph(attributePaths = "persona")
    List<Empleado> findAllByOrderByIdEmpleadoAsc();
}
