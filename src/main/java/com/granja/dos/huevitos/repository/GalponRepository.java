package com.granja.dos.huevitos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.granja.dos.huevitos.models.infrastructure.Galpon;

@Repository
public interface GalponRepository extends JpaRepository<Galpon, Integer> {
    List<Galpon> findAllByEstadoIgnoreCaseOrderByNombreAsc(String estado);
    List<Galpon> findBySectorIdSector(Integer idSector);
}
