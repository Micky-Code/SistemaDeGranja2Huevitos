package com.granja.dos.huevitos.controller;

import com.granja.dos.huevitos.dto.AnalisisGalponDTO;
import com.granja.dos.huevitos.dto.ProduccionRegistroDTO.*;
import com.granja.dos.huevitos.service.ProduccionRegistroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/produccion/registro")
@RequiredArgsConstructor
public class ProduccionRegistroController {
    private final ProduccionRegistroService service;

    @GetMapping("/tipos-huevo")
    public List<TipoResponse> tipos() { return service.listarTipos(); }

    @PostMapping("/tipos-huevo")
    @ResponseStatus(HttpStatus.CREATED)
    public TipoResponse crearTipo(@Valid @RequestBody TipoRequest request) { return service.crearTipo(request); }

    @PutMapping("/tipos-huevo/{id}")
    public TipoResponse editarTipo(@PathVariable Integer id, @Valid @RequestBody TipoRequest request) {
        return service.editarTipo(id, request);
    }

    @GetMapping
    public List<RegistroResponse> listar() { return service.listar(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroResponse registrar(@Valid @RequestBody RegistroRequest request) { return service.registrar(request); }

    @GetMapping("/sectores")
    public List<SectorResponse> sectores() { return service.resumen(); }

    @GetMapping("/indicadores/{id}")
    public AnalisisGalponDTO indicadores(@PathVariable Integer id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(defaultValue = "false") boolean masProductivo) {
        return service.analisis(id, fecha, masProductivo);
    }
}
