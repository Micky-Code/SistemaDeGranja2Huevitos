package com.granja.dos.huevitos.service;

import java.util.List;

import com.granja.dos.huevitos.dto.SectorRequestDTO;
import com.granja.dos.huevitos.dto.SectorResponseDTO;

public interface SectorService {
    List<SectorResponseDTO> listarTodos();
    SectorResponseDTO guardarSector(SectorRequestDTO request);
    void eliminarSector(Integer id);
    SectorResponseDTO actualizarSector(Integer id, SectorRequestDTO request);
}
