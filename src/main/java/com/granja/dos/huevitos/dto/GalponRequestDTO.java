package com.granja.dos.huevitos.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GalponRequestDTO {
    private String nombre;
    private Integer capacidad;
    private String estado;
    private Integer sectorId;
}