package com.granja.dos.huevitos.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.granja.dos.huevitos.dto.SectorRequestDTO;
import com.granja.dos.huevitos.dto.SectorResponseDTO;
import com.granja.dos.huevitos.service.SectorService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/infraestructura/sectores")
@RequiredArgsConstructor
public class SectorController {
    private final SectorService sectorService;

    @GetMapping
    public ResponseEntity<List<SectorResponseDTO>> listar() {
        return ResponseEntity.ok(sectorService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<?> guardar(@RequestBody SectorRequestDTO request) {
        try {
            SectorResponseDTO nuevoSector = sectorService.guardarSector(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoSector);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody SectorRequestDTO request) {
        try {
            SectorResponseDTO sectorActualizado = sectorService.actualizarSector(id, request);
            return ResponseEntity.ok(sectorActualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            sectorService.eliminarSector(id);
            return ResponseEntity.ok(Map.of("message", "Sector eliminado correctamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
