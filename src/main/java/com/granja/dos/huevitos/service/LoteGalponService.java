package com.granja.dos.huevitos.service;

import com.granja.dos.huevitos.dto.LoteGalponRequestDTO;
import com.granja.dos.huevitos.models.infrastructure.LoteGalpon;
import com.granja.dos.huevitos.service.LoteGalponService;
public interface LoteGalponService {
    LoteGalpon asignarLoteAGalpon(LoteGalponRequestDTO dto);
}