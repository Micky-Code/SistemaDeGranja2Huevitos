package com.granja.dos.huevitos.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.granja.dos.huevitos.dto.GalponRequestDTO;
import com.granja.dos.huevitos.dto.GalponResponseDTO;
import com.granja.dos.huevitos.service.GalponService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/infraestructura/galpones")
@RequiredArgsConstructor // Genera el constructor inmutable para la dependencia
public class GalponController {

    // Dependencia final. Protegida contra modificaciones en tiempo de ejecución.
    private final GalponService galponService;

    // Endpoint GET: Expone estrictamente el DTO para proteger la base de datos
    @GetMapping
    public ResponseEntity<List<GalponResponseDTO>> listarGalpones() {
        return ResponseEntity.ok(galponService.listarGalpones());
    }

    // Endpoint POST: Recibe el DTO de entrada y retorna el DTO de salida con HTTP 201
    @PostMapping
    public ResponseEntity<GalponResponseDTO> guardarGalpon(@RequestBody GalponRequestDTO dto) {
        GalponResponseDTO nuevoGalpon = galponService.guardarGalpon(
            dto.getNombre(), 
            dto.getCapacidad(), 
            dto.getEstado(), 
            dto.getSectorId()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoGalpon);
    }
}