package com.granja.dos.huevitos.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoteAvesRequestDTO {
    private String nombre;
    private Integer cantidadInicial;
    private Integer cantidadActual;
    private LocalDate fechaIngreso;
}