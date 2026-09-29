package com.granja.dos.huevitos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.granja.dos.huevitos.models.infrastructure.LoteGalpon;

@Repository
public interface LoteGalponRepository extends JpaRepository<LoteGalpon, Integer> {
}