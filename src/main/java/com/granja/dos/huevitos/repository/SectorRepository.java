package com.granja.dos.huevitos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.granja.dos.huevitos.models.infrastructure.Sector;

@Repository
public interface SectorRepository extends JpaRepository<Sector, Integer> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdSectorNot(String nombre, Integer idSector);
}
