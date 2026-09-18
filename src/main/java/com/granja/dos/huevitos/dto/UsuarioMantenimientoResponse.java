package com.granja.dos.huevitos.dto;

public record UsuarioMantenimientoResponse(Integer idUsuario, String username, Integer idRol,
        String rol, Boolean estado) {
}
