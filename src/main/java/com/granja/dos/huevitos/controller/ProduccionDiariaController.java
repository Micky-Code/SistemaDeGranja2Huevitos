package com.granja.dos.huevitos.controller;

import com.granja.dos.huevitos.dto.ProduccionDiariaRequestDTO;
import com.granja.dos.huevitos.dto.ProduccionDiariaResponseDTO;
import com.granja.dos.huevitos.service.ProduccionDiariaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produccion")
@RequiredArgsConstructor
public class ProduccionDiariaController {

    private final ProduccionDiariaService produccionDiariaService;

    @PostMapping
    public ResponseEntity<ProduccionDiariaResponseDTO> registrarProduccion(@RequestBody ProduccionDiariaRequestDTO requestDTO) {
        return ResponseEntity.ok(produccionDiariaService.registrarProduccion(requestDTO));
    }

    @GetMapping
    public ResponseEntity<List<ProduccionDiariaResponseDTO>> listarProducciones() {
        return ResponseEntity.ok(produccionDiariaService.listarProducciones());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProduccionDiariaResponseDTO> obtenerProduccionPorId(@PathVariable Long id) {
        return ResponseEntity.ok(produccionDiariaService.obtenerProduccionPorId(id));
    }
    
    @GetMapping("/analisis/{idGalpon}")
    public ResponseEntity<com.granja.dos.huevitos.dto.AnalisisGalponDTO> obtenerAnalisisGalpon(@PathVariable Integer idGalpon) {
        return ResponseEntity.ok(produccionDiariaService.obtenerAnalisisGalpon(idGalpon));
    }
}
