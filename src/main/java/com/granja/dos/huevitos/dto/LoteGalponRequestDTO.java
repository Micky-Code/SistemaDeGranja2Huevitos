package com.granja.dos.huevitos.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoteGalponRequestDTO {
    private Integer loteId;
    private Integer galponId;
    private Integer cantidadAves;
    private LocalDate fechaIngreso;
}