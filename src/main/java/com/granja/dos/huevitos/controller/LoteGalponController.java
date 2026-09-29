package com.granja.dos.huevitos.controller;

import com.granja.dos.huevitos.dto.LoteGalponRequestDTO;
import com.granja.dos.huevitos.models.infrastructure.LoteGalpon;
import com.granja.dos.huevitos.service.LoteGalponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/infraestructura/lotes-galpon")
public class LoteGalponController {

    @Autowired
    private LoteGalponService loteGalponService;

    @PostMapping
    public ResponseEntity<?> asignarLoteAGalpon(@RequestBody LoteGalponRequestDTO dto) {
        LoteGalpon asignacion = loteGalponService.asignarLoteAGalpon(dto);
        return ResponseEntity.ok(asignacion);
    }
}