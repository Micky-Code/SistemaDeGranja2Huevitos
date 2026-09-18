package com.granja.dos.huevitos.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecepcionAlmacenRequest(
        @NotNull(message = "Debe indicar la producción de galpón que llega a almacén.") Integer idProduccion,
        @NotNull(message = "La fecha de recepción es obligatoria.") LocalDate fecha,
        @NotNull(message = "Debe indicar el empleado responsable de almacén.") Integer idEmpleado,
        @Size(max = 255, message = "La observación no puede superar los 255 caracteres.") String observacion,
        @NotEmpty(message = "Debe registrar al menos una clasificación recibida.") @Valid List<DetalleRecepcionRequest> detalles) {
}
