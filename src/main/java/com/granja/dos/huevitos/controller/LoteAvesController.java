package com.granja.dos.huevitos.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.granja.dos.huevitos.dto.LoteAvesRequestDTO;
import com.granja.dos.huevitos.dto.LoteAvesResponseDTO;
import com.granja.dos.huevitos.service.LoteAvesService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/infraestructura/lotes")
@RequiredArgsConstructor // Genera un constructor inmutable para los atributos 'final'
public class LoteAvesController {

    // Dependencia inmutable. Adiós al @Autowired de campo.
    private final LoteAvesService loteAvesService;

    // Endpoint GET: Aislado con DTO de respuesta
 // Endpoint GET: Aislado con DTO de respuesta
    @GetMapping
    public ResponseEntity<List<LoteAvesResponseDTO>> listarLotes() {
        return ResponseEntity.ok(loteAvesService.listarLotes());
    }
    // Endpoint POST: Aislado con DTOs de entrada y salida
    @PostMapping
    public ResponseEntity<LoteAvesResponseDTO> guardarLote(@RequestBody LoteAvesRequestDTO request) {
        LoteAvesResponseDTO nuevoLote = loteAvesService.guardarLote(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoLote);
    }
}