package com.granja.dos.huevitos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.granja.dos.huevitos.models.reportes.DetalleReporte;

public interface DetalleReporteRepository extends JpaRepository<DetalleReporte, Integer> {
}
