package com.granja.dos.huevitos.repository;

import com.granja.dos.huevitos.models.infrastructure.LoteAves;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoteAvesRepository extends JpaRepository<LoteAves, Integer> {
    // Si en el futuro necesitas buscar por código de lote exacto:
    // Optional<LoteAves> findByNombre(String nombre);
}