package com.granja.dos.huevitos.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.granja.dos.huevitos.dto.LoteGalponRequestDTO; // Asegúrate de tener este DTO creado
import com.granja.dos.huevitos.models.infrastructure.Galpon;
import com.granja.dos.huevitos.models.infrastructure.LoteAves;
import com.granja.dos.huevitos.models.infrastructure.LoteGalpon;
import com.granja.dos.huevitos.repository.GalponRepository;
import com.granja.dos.huevitos.repository.LoteAvesRepository;
import com.granja.dos.huevitos.repository.LoteGalponRepository;
import com.granja.dos.huevitos.service.LoteGalponService;

@Service
public class LoteGalponServiceImpl implements LoteGalponService{

    @Autowired
    private LoteGalponRepository loteGalponRepository;

    @Autowired
    private GalponRepository galponRepository;

    @Autowired
    private LoteAvesRepository loteAvesRepository;

    public LoteGalpon asignarLoteAGalpon(LoteGalponRequestDTO dto) {
        Galpon galpon = galponRepository.findById(dto.getGalponId())
            .orElseThrow(() -> new RuntimeException("Galpón no encontrado"));

        LoteAves lote = loteAvesRepository.findById(dto.getLoteId())
            .orElseThrow(() -> new RuntimeException("Lote no encontrado"));

        if (dto.getCantidadAves() > galpon.getCapacidad()) {
            throw new IllegalArgumentException("La cantidad de aves excede la capacidad máxima del galpón.");
        }

        LoteGalpon loteGalpon = new LoteGalpon();
        loteGalpon.setGalpon(galpon);
        loteGalpon.setLote(lote);
        loteGalpon.setCantidadAves(dto.getCantidadAves());
        loteGalpon.setFechaIngreso(dto.getFechaIngreso());

        return loteGalponRepository.save(loteGalpon);
    }
}