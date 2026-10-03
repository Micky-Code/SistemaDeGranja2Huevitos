package com.granja.dos.huevitos.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AnalisisGalponDTO {
    private Integer idGalpon;
    private String nombreGalpon;
    private String nombreSector;
    private Integer cantidadGallinas;
    private Double promedioProduccionDiaria;
    private Integer produccionAyer;
    private LocalDate fechaConsultada;
    private Long produccionFecha;
    private Double promedioProduccionMes;
    private String estadoProduccion;
}
