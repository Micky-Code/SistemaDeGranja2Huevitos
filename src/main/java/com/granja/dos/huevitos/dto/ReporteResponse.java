package com.granja.dos.huevitos.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ReporteResponse(
        Integer idReporte,
        String tipo,
        LocalDateTime fechaGeneracion,
        String generadoPor,
        String parametros,
        List<DetalleReporteResponse> detalles) {
}
