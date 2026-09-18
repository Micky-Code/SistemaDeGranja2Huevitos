package com.granja.dos.huevitos.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.granja.dos.huevitos.dto.ReporteResponse;
import com.granja.dos.huevitos.service.ReporteService;

/**
 * API del módulo "9. Reportes" (obligatorio para todos los módulos).
 */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping
    public List<ReporteResponse> listar() {
        return reporteService.listarReportes();
    }

    @PostMapping("/almacen-mermas")
    @ResponseStatus(HttpStatus.CREATED)
    public ReporteResponse generarReporteMermas(Authentication authentication) {
        return reporteService.generarReporteMermasAlmacen(authentication.getName());
    }
}
