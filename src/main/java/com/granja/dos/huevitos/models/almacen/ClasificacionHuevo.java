package com.granja.dos.huevitos.models.almacen;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Catálogo de clasificación de huevos usado en Almacén
 * (Rojo, Pardo, Jumbo, Doble Yema, Poroso, Roto).
 */
@Entity
@Table(name = "clasificacion_huevo", schema = "Avicola")
@Data
@NoArgsConstructor
public class ClasificacionHuevo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_clasificacion")
    private Integer idClasificacion;

    @Column(name = "nombre", length = 30, nullable = false, unique = true)
    private String nombre;

    @Column(name = "descripcion", length = 200)
    private String descripcion;

    @Column(name = "creat", nullable = false)
    private LocalDateTime creat = LocalDateTime.now();

    public ClasificacionHuevo(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }
}
