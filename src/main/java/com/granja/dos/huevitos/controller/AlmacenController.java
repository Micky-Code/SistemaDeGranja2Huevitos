package com.granja.dos.huevitos.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.granja.dos.huevitos.dto.ClasificacionHuevoResponse;
import com.granja.dos.huevitos.dto.RecepcionAlmacenRequest;
import com.granja.dos.huevitos.dto.RecepcionAlmacenResponse;
import com.granja.dos.huevitos.service.AlmacenService;

import jakarta.validation.Valid;

/**
 * API del módulo "7. Almacén y Clasificación Final".
 */
@RestController
@RequestMapping("/api/almacen")
public class AlmacenController {

    private final AlmacenService almacenService;

    public AlmacenController(AlmacenService almacenService) {
        this.almacenService = almacenService;
    }

    @GetMapping("/clasificaciones")
    public List<ClasificacionHuevoResponse> clasificaciones() {
        return almacenService.listarClasificaciones();
    }

    @GetMapping("/recepciones")
    public List<RecepcionAlmacenResponse> listar() {
        return almacenService.listarRecepciones();
    }

    @PostMapping("/recepciones")
    @ResponseStatus(HttpStatus.CREATED)
    public RecepcionAlmacenResponse registrar(@Valid @RequestBody RecepcionAlmacenRequest request) {
        return almacenService.registrarRecepcion(request);
    }
}
