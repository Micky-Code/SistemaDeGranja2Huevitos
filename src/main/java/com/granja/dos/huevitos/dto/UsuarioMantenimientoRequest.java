package com.granja.dos.huevitos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioMantenimientoRequest(
        @NotBlank @Size(max = 50) String username,
        String password,
        @NotNull Integer idRol,
        @NotNull Boolean estado) {
}
