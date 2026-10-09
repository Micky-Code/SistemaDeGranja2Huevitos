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
    public ResponseEntity<?> guardarLote(@RequestBody LoteAvesRequestDTO request) {
        try {
            LoteAvesResponseDTO nuevoLote = loteAvesService.guardarLote(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoLote);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of("message", "Error: Ya existe un lote con esa guía (código de lote), o faltan datos obligatorios."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("message", "Error interno al guardar el lote: " + e.getMessage()));
        }
    }

    // Endpoint GET: Simular consulta de guía a base de datos externa usando un JSON de prueba
    @GetMapping("/verificar-guia")
    public ResponseEntity<LoteAvesResponseDTO> verificarGuia(@org.springframework.web.bind.annotation.RequestParam String guia) {
        if (guia == null || !guia.matches("\\d{3}-\\d{4}")) {
            return ResponseEntity.badRequest().build();
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            java.io.InputStream is = getClass().getResourceAsStream("/guias_mock.json");
            
            if (is == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            List<LoteAvesResponseDTO> mockData = mapper.readValue(is, new com.fasterxml.jackson.core.type.TypeReference<List<LoteAvesResponseDTO>>() {});
            
            java.util.Optional<LoteAvesResponseDTO> encontrado = mockData.stream()
                .filter(l -> guia.equals(l.getNombre()))
                .findFirst();

            if (encontrado.isPresent()) {
                return ResponseEntity.ok(encontrado.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}