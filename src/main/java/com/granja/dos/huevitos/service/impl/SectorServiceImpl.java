package com.granja.dos.huevitos.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;

import com.granja.dos.huevitos.dto.SectorRequestDTO;
import com.granja.dos.huevitos.dto.SectorResponseDTO;
import com.granja.dos.huevitos.models.infrastructure.Sector;
import com.granja.dos.huevitos.repository.SectorRepository;
import com.granja.dos.huevitos.service.SectorService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SectorServiceImpl implements SectorService {
    
    private final SectorRepository sectorRepository;
    
    @Override
    public List<SectorResponseDTO> listarTodos() {
        List<Sector> sectoresDB = sectorRepository.findAll();
        
        return sectoresDB.stream().map(sector -> {
            SectorResponseDTO dto = new SectorResponseDTO();
            dto.setIdSector(sector.getIdSector());
            dto.setNombre(sector.getNombre());
            dto.setDescripcion(sector.getDescripcion());
            return dto;
        }).toList();
    }
    
    @Override
    public SectorResponseDTO guardarSector(SectorRequestDTO request) {
        // 1. Mapeo de entrada: DTO a Entidad (Aislamiento de la base de datos)
        Sector nuevoSector = new Sector();
        nuevoSector.setNombre(request.getNombre());
        nuevoSector.setDescripcion(request.getDescripcion());

        // 2. Persistencia física
        Sector sectorGuardado = sectorRepository.save(nuevoSector);

        // 3. Mapeo de salida: Entidad a DTO (Contrato hacia el frontend)
        SectorResponseDTO dto = new SectorResponseDTO();
        dto.setIdSector(sectorGuardado.getIdSector());
        dto.setNombre(sectorGuardado.getNombre());
        dto.setDescripcion(sectorGuardado.getDescripcion());
        
        return dto;
    }
}