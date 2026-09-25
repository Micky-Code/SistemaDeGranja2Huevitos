package com.granja.dos.huevitos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.granja.dos.huevitos.models.infrastructure.Galpon;

public interface GalponRepository extends JpaRepository<Galpon, Integer> {
    List<Galpon> findAllByEstadoIgnoreCaseOrderByNombreAsc(String estado);
}
