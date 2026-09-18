package com.granja.dos.huevitos.dto;

public record DetalleRecepcionResponse(
        Integer idDetalle,
        Integer idClasificacion,
        String clasificacion,
        Integer cantidad,
        Integer cantidadRota,
        Integer cantidadBuena) {
}
