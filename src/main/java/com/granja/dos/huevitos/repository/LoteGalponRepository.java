package com.granja.dos.huevitos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.granja.dos.huevitos.models.infrastructure.LoteGalpon;

import java.util.List;

@Repository
public interface LoteGalponRepository extends JpaRepository<LoteGalpon, Integer> {
    List<LoteGalpon> findByGalpon_IdGalpon(Integer idGalpon);
}