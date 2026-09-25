package com.granja.dos.huevitos.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.granja.dos.huevitos.dto.EmpleadoMantenimientoRequest;
import com.granja.dos.huevitos.dto.EmpleadoMantenimientoResponse;
import com.granja.dos.huevitos.dto.GalponResumenResponse;
import com.granja.dos.huevitos.dto.TipoDocumentoResponse;
import com.granja.dos.huevitos.service.EmpleadoMantenimientoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {
    private final EmpleadoMantenimientoService service;

    public EmpleadoController(EmpleadoMantenimientoService service) {
        this.service = service;
    }

    @GetMapping
    public List<EmpleadoMantenimientoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/galpones")
    public List<GalponResumenResponse> listarGalpones() {
        return service.listarGalpones();
    }

    @GetMapping("/tipos-documento")
    public List<TipoDocumentoResponse> listarTiposDocumento() {
        return service.listarTiposDocumento();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmpleadoMantenimientoResponse crear(@Valid @RequestBody EmpleadoMantenimientoRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    public EmpleadoMantenimientoResponse actualizar(@PathVariable Integer id,
            @Valid @RequestBody EmpleadoMantenimientoRequest request) {
        return service.actualizar(id, request);
    }
}
