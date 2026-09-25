package com.granja.dos.huevitos.dto;

import java.time.LocalDate;
import java.util.List;

import com.granja.dos.huevitos.models.personal.TipoAsignacion;
import com.granja.dos.huevitos.models.personal.TurnoGuardia;

public record EmpleadoMantenimientoResponse(
        Integer idEmpleado, String nombres, String apellidos, LocalDate fechaNacimiento, String sexo,
        String correo, String telefono, String direccion, Boolean estado, LocalDate fechaIngreso,
        LocalDate fechaSalida, DocumentoResponse documento, GuardiaResponse guardia,
        List<AsignacionResponse> asignaciones) {

    public record DocumentoResponse(Integer idDocumento, Integer idTipoDocumento, String tipoCodigo,
            String tipoNombre, String numeroDocumento) {
    }

    public record GuardiaResponse(Integer idGuardia, TurnoGuardia turno, LocalDate fechaInicio, LocalDate fechaFin) {
    }

    public record AsignacionResponse(Integer idAsignacion, Integer idGalpon, String galpon,
            TipoAsignacion tipoAsignacion, LocalDate fechaInicio, LocalDate fechaFin) {
    }
}
