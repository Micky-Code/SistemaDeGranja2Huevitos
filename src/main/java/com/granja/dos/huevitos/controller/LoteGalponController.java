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
        try {
            LoteGalpon asignacion = loteGalponService.asignarLoteAGalpon(dto);
            return ResponseEntity.ok(java.util.Map.of(
                "message", "Asignación exitosa",
                "id", asignacion.getIdLoteGalpon()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<?> listarTodos() {
        return ResponseEntity.ok(loteGalponService.listarTodos().stream().map(lg -> java.util.Map.of(
            "idLoteGalpon", lg.getIdLoteGalpon(),
            "loteId", lg.getLote().getIdLote(),
            "galponId", lg.getGalpon().getIdGalpon(),
            "cantidadAves", lg.getCantidadAves(),
            "fechaIngreso", lg.getFechaIngreso()
        )).toList());
    }
}