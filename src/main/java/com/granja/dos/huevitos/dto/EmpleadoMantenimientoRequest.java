package com.granja.dos.huevitos.dto;

import java.time.LocalDate;

import com.granja.dos.huevitos.models.personal.TipoAsignacion;
import com.granja.dos.huevitos.models.personal.TurnoGuardia;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EmpleadoMantenimientoRequest(
        @NotBlank @Size(max = 100) String nombres,
        @NotBlank @Size(max = 100) String apellidos,
        LocalDate fechaNacimiento,
        @Pattern(regexp = "M|F", message = "El sexo debe ser M o F.") String sexo,
        @Email @Size(max = 150) String correo,
        @Size(max = 20) String telefono,
        @Size(max = 200) String direccion,
        @NotNull Boolean estado,
        @NotNull @Positive Integer idTipoDocumento,
        @NotBlank @Size(max = 20) String numeroDocumento,
        @NotNull LocalDate fechaIngreso,
        LocalDate fechaSalida,
        @Valid GuardiaRequest guardia,
        @Valid AsignacionRequest asignacion) {

    public record GuardiaRequest(@NotNull TurnoGuardia turno, @NotNull LocalDate fechaInicio, LocalDate fechaFin) {
    }

    public record AsignacionRequest(Integer idAsignacion, @NotNull @Positive Integer idGalpon,
            @NotNull TipoAsignacion tipoAsignacion, @NotNull LocalDate fechaInicio, LocalDate fechaFin) {
    }
}
