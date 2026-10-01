package com.granja.dos.huevitos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public final class ProduccionRegistroDTO {
    private ProduccionRegistroDTO() {}

    public record TipoRequest(
            @NotBlank @Size(max = 100) String nombre,
            String descripcion) {}
    public record TipoResponse(Integer idTipo, String nombre, String descripcion) {}
    public record DetalleRequest(@NotNull @Positive Integer tipoHuevoId,
                                 @NotNull @PositiveOrZero Integer cantidad) {}
    public record RegistroRequest(@NotNull LocalDate fecha,
                                  @NotNull @Positive Integer galponId,
                                  String observacion,
                                  @NotEmpty List<@NotNull @Valid DetalleRequest> detalles) {}
    public record DetalleResponse(Integer tipoHuevoId, String nombre, Integer cantidad) {}
    public record RegistroResponse(Integer idProduccion, LocalDate fecha, Integer galponId,
                                   String galpon, String sector, Integer totalHuevos,
                                   String observacion, List<DetalleResponse> detalles) {}
    public record SectorResponse(LocalDate fecha, Integer sectorId, String sector, Long totalHuevos) {}
}
