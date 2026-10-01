package com.granja.dos.huevitos.repository;

import com.granja.dos.huevitos.models.produccion.DetalleProduccion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleProduccionRepository extends JpaRepository<DetalleProduccion, Integer> {
}
