package com.granja.dos.huevitos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.granja.dos.huevitos.models.almacen.DetalleRecepcion;

public interface DetalleRecepcionRepository extends JpaRepository<DetalleRecepcion, Integer> {
}
