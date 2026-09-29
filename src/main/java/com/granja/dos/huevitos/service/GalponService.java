package com.granja.dos.huevitos.service;

import java.util.List;
import com.granja.dos.huevitos.dto.GalponResponseDTO;

public interface GalponService {
    List<GalponResponseDTO> listarGalpones();
    
    // El contrato obliga a la implementación a devolver el DTO seguro
    GalponResponseDTO guardarGalpon(String nombre, Integer capacidad, String estado, Integer idSector);
}