package com.granja.dos.huevitos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DetalleRecepcionRequest(
        @NotNull(message = "La clasificación del huevo es obligatoria.") Integer idClasificacion,
        @NotNull(message = "La cantidad recibida es obligatoria.") @Min(value = 0, message = "La cantidad no puede ser negativa.") Integer cantidad,
        @NotNull(message = "La cantidad rota es obligatoria (usa 0 si no hubo roturas).") @Min(value = 0, message = "La cantidad rota no puede ser negativa.") Integer cantidadRota) {
}
