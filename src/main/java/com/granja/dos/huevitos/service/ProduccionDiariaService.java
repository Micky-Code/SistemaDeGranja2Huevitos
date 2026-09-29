package com.granja.dos.huevitos.service;

import com.granja.dos.huevitos.dto.ProduccionDiariaRequestDTO;
import com.granja.dos.huevitos.dto.ProduccionDiariaResponseDTO;

import java.util.List;

import com.granja.dos.huevitos.dto.AnalisisGalponDTO;

public interface ProduccionDiariaService {
    ProduccionDiariaResponseDTO registrarProduccion(ProduccionDiariaRequestDTO requestDTO);
    List<ProduccionDiariaResponseDTO> listarProducciones();
    ProduccionDiariaResponseDTO obtenerProduccionPorId(Long id);
    AnalisisGalponDTO obtenerAnalisisGalpon(Integer idGalpon);
}
