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
        
        return sectoresDB.stream().map(this::mapToDTO).toList();
    }
    
    @Override
    public SectorResponseDTO guardarSector(SectorRequestDTO request) {
        // Validar nombre único
        if (sectorRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new IllegalArgumentException("Ya existe un sector con el nombre: " + request.getNombre());
        }

        Sector nuevoSector = new Sector();
        nuevoSector.setNombre(request.getNombre());

        Sector sectorGuardado = sectorRepository.save(nuevoSector);
        return mapToDTO(sectorGuardado);
    }

    @Override
    public void eliminarSector(Integer id) {
        Sector sector = sectorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el sector con ID: " + id));

        // Validar que el sector no tenga galpones asociados
        if (sector.getGalpones() != null && !sector.getGalpones().isEmpty()) {
            throw new IllegalStateException(
                    "No se puede eliminar el sector '" + sector.getNombre() +
                    "' porque tiene " + sector.getGalpones().size() + " galpón(es) asociado(s). " +
                    "Elimine o reasigne los galpones primero.");
        }

        sectorRepository.delete(sector);
    }

    @Override
    public SectorResponseDTO actualizarSector(Integer id, SectorRequestDTO request) {
        Sector sector = sectorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el sector con ID: " + id));

        // Validar nombre único (excluyendo el sector actual)
        if (sectorRepository.existsByNombreIgnoreCaseAndIdSectorNot(request.getNombre(), id)) {
            throw new IllegalArgumentException("Ya existe otro sector con el nombre: " + request.getNombre());
        }

        sector.setNombre(request.getNombre());

        Sector sectorActualizado = sectorRepository.save(sector);
        return mapToDTO(sectorActualizado);
    }

    private SectorResponseDTO mapToDTO(Sector sector) {
        SectorResponseDTO dto = new SectorResponseDTO();
        dto.setIdSector(sector.getIdSector());
        dto.setNombre(sector.getNombre());
        return dto;
    }
}