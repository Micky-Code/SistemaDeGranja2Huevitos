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

import com.granja.dos.huevitos.dto.RolResponse;
import com.granja.dos.huevitos.dto.UsuarioMantenimientoRequest;
import com.granja.dos.huevitos.dto.UsuarioMantenimientoResponse;
import com.granja.dos.huevitos.service.UsuarioMantenimientoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioMantenimientoService service;

    public UsuarioController(UsuarioMantenimientoService service) {
        this.service = service;
    }

    @GetMapping
    public List<UsuarioMantenimientoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/roles")
    public List<RolResponse> roles() {
        return service.listarRoles();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioMantenimientoResponse crear(@Valid @RequestBody UsuarioMantenimientoRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    public UsuarioMantenimientoResponse actualizar(@PathVariable Integer id,
            @Valid @RequestBody UsuarioMantenimientoRequest request) {
        return service.actualizar(id, request);
    }
}
