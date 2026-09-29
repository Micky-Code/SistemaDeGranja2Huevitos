package com.granja.dos.huevitos.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoteGalponRequestDTO {
    private Integer loteId;
    private Integer galponId;
    private Integer cantidadAves;
    private LocalDateTime fechaIngreso;
}