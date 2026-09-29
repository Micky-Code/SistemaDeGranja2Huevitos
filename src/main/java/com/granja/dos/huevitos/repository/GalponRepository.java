package com.granja.dos.huevitos.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.granja.dos.huevitos.models.infrastructure.Galpon;

@Repository
public interface GalponRepository extends JpaRepository<Galpon, Integer> {
    // Método derivado para buscar galpones por el ID de su sector (útil para las cascadas)
    List<Galpon> findBySectorIdSector(Integer idSector);

    List<Galpon> findAllByEstadoIgnoreCaseOrderByNombreAsc(String estado);
}
