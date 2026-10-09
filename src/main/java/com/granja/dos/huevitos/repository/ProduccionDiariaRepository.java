package com.granja.dos.huevitos.repository;

import com.granja.dos.huevitos.models.produccion.ProduccionDiaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.granja.dos.huevitos.models.infrastructure.LoteGalpon;
import java.util.List;

@Repository
public interface ProduccionDiariaRepository extends JpaRepository<ProduccionDiaria, Long> {
    List<ProduccionDiaria> findByLoteGalponIn(List<LoteGalpon> loteGalpones);
    boolean existsByLoteGalpon_IdLoteGalponAndFecha(Integer idLoteGalpon, java.time.LocalDate fecha);
}
