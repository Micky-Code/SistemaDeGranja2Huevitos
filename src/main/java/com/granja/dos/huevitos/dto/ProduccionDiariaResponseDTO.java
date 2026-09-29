package com.granja.dos.huevitos.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ProduccionDiariaResponseDTO {
    private Long id;
    private LocalDate fecha;
    private Integer cantidadHuevosBuenos;
    private Integer cantidadHuevosRotos;
    private Integer cantidadHuevosSucios;
    private String observaciones;
    private Integer loteGalponId;
}
