package com.granja.dos.huevitos.repository;

import com.granja.dos.huevitos.models.produccion.ProduccionGalpon;
import com.granja.dos.huevitos.models.infrastructure.Galpon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProduccionGalponRepository extends JpaRepository<ProduccionGalpon, Integer> {
    boolean existsByGalpon_IdGalponAndFecha(Integer idGalpon, LocalDate fecha);
    List<ProduccionGalpon> findByGalpon_IdGalpon(Integer idGalpon);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from Galpon g where g.idGalpon = :id")
    Optional<Galpon> bloquearGalpon(@Param("id") Integer id);
}
