package com.granja.dos.huevitos.dto;

import lombok.Data;

@Data
public class AnalisisGalponDTO {
    private Integer idGalpon;
    private String nombreGalpon;
    private String nombreSector;
    private Integer cantidadGallinas;
    private Double promedioProduccionDiaria;
    private Integer produccionAyer;
    private Double promedioProduccionMes;
    private String estadoProduccion;
}
