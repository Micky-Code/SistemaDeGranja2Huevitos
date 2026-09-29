package com.granja.dos.huevitos.service;

import java.util.List;

import com.granja.dos.huevitos.dto.SectorResponseDTO;

public interface SectorService {
    List<SectorResponseDTO> listarTodos();
    SectorResponseDTO guardarSector(com.granja.dos.huevitos.dto.SectorRequestDTO request);
}
