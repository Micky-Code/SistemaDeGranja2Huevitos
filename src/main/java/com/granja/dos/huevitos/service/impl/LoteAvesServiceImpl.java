package com.granja.dos.huevitos.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.granja.dos.huevitos.dto.LoteAvesRequestDTO;
import com.granja.dos.huevitos.dto.LoteAvesResponseDTO;
import com.granja.dos.huevitos.models.infrastructure.LoteAves;
import com.granja.dos.huevitos.repository.LoteAvesRepository;
import com.granja.dos.huevitos.service.LoteAvesService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoteAvesServiceImpl implements LoteAvesService {

    private final LoteAvesRepository loteAvesRepository;

    @Override
    public List<LoteAvesResponseDTO> listarLotes() {
        // 1. Extracción de base de datos
        List<LoteAves> lotesDB = loteAvesRepository.findAll();

        // 2. Mapeo seguro a memoria plana
        return lotesDB.stream().map(lote -> {
            LoteAvesResponseDTO dto = new LoteAvesResponseDTO();
            dto.setIdLote(lote.getIdLote());
            dto.setNombre(lote.getNombre());
            dto.setCantidadInicial(lote.getCantidadInicial());
            dto.setCantidadActual(lote.getCantidadActual());
            
            // Transformación estructural de LocalDateTime a LocalDate
            if (lote.getFechaIngreso() != null) {
                dto.setFechaIngreso(lote.getFechaIngreso().toLocalDate());
            }
            return dto;
        }).toList();
    }

    @Override
    public LoteAvesResponseDTO guardarLote(LoteAvesRequestDTO request) {
        // 1. Mapeo de entrada: Aislamiento del DTO hacia la Entidad
        LoteAves nuevoLote = new LoteAves();
        nuevoLote.setNombre(request.getNombre());
        nuevoLote.setCantidadInicial(request.getCantidadInicial());
        nuevoLote.setCantidadActual(request.getCantidadActual()); 
        
        // Conversión estricta inyectando la hora base para PostgreSQL
        if (request.getFechaIngreso() != null) {
            nuevoLote.setFechaIngreso(request.getFechaIngreso().atStartOfDay());
        }
        
        // 2. Persistencia física
        LoteAves loteGuardado = loteAvesRepository.save(nuevoLote);

        // 3. Mapeo de salida: Entidad hacia el DTO de respuesta
        LoteAvesResponseDTO dto = new LoteAvesResponseDTO();
        dto.setIdLote(loteGuardado.getIdLote());
        dto.setNombre(loteGuardado.getNombre());
        dto.setCantidadInicial(loteGuardado.getCantidadInicial());
        dto.setCantidadActual(loteGuardado.getCantidadActual());
        
        dto.setFechaIngreso(loteGuardado.getFechaIngreso() != null ? loteGuardado.getFechaIngreso().toLocalDate() : null);
        
        // 4. Cierre de contrato de memoria
        return dto;
    }
}