package com.granja.dos.huevitos.service;

import java.util.List;
import com.granja.dos.huevitos.dto.LoteAvesRequestDTO;
import com.granja.dos.huevitos.dto.LoteAvesResponseDTO;

public interface LoteAvesService {
    List<LoteAvesResponseDTO> listarLotes();
    LoteAvesResponseDTO guardarLote(LoteAvesRequestDTO request);
}	