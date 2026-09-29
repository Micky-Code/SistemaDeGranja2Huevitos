package com.granja.dos.huevitos.controller;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.granja.dos.huevitos.dto.SectorResponseDTO;
import com.granja.dos.huevitos.service.SectorService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/infraestructura/sectores") // <-- Agrega el segmento faltante
@RequiredArgsConstructor
public class SectorController {
	private final SectorService sectorService;
	
	@GetMapping
	public ResponseEntity<List<SectorResponseDTO>> listar() {
	    return ResponseEntity.ok(sectorService.listarTodos());
	}
	@PostMapping
	public ResponseEntity<SectorResponseDTO> guardar(@RequestBody com.granja.dos.huevitos.dto.SectorRequestDTO request) {
	    SectorResponseDTO nuevoSector = sectorService.guardarSector(request);
	    return ResponseEntity.status(HttpStatus.CREATED).body(nuevoSector);
	}
}
