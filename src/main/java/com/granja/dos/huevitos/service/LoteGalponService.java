package com.granja.dos.huevitos.service;

import com.granja.dos.huevitos.dto.LoteGalponRequestDTO;
import com.granja.dos.huevitos.models.infrastructure.LoteGalpon;
import com.granja.dos.huevitos.service.LoteGalponService;
import java.util.List;

public interface LoteGalponService {
    LoteGalpon asignarLoteAGalpon(LoteGalponRequestDTO dto);
    List<LoteGalpon> listarTodos();
}