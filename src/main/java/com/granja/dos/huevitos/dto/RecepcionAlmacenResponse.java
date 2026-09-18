package com.granja.dos.huevitos.dto;

import java.time.LocalDate;
import java.util.List;

public record RecepcionAlmacenResponse(
        Integer idRecepcion,
        Integer idProduccion,
        LocalDate fecha,
        Integer idEmpleado,
        String observacion,
        int totalRecibido,
        int mermaTraslado,
        double porcentajeMerma,
        List<DetalleRecepcionResponse> detalles) {
}
