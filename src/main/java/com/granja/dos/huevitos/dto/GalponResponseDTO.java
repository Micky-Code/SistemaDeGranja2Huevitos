package com.granja.dos.huevitos.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GalponResponseDTO {
    private Integer idGalpon;
    private String nombre;
    private Integer capacidad;
    private String estado;
    private Integer sectorId; // Solo mandamos el ID, no el objeto Sector entero
    private String nombreSector; // Y el nombre para la tabla HTML
}