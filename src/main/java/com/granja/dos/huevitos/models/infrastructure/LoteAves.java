package com.granja.dos.huevitos.models.infrastructure;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lotes_aves", schema = "avicola") 
@Getter @Setter
@NoArgsConstructor
public class LoteAves {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lote")
    private Integer idLote;

    // El puente: Java lo conoce como 'nombre', la BD como 'codigo_lote'
    @Column(name = "codigo_lote", length = 50, nullable = false, unique = true)
    private String nombre;

    @Column(name = "cantidad_inicial", nullable = false)
    private Integer cantidadInicial;

    @Column(name = "cantidad_actual", nullable = false)
    private Integer cantidadActual;

    @Column(name = "estado", nullable = false)
    private Boolean estado = true;

    // El puente: Mapeamos la variable 'fechaIngreso' con la columna física 'creat'
    @Column(name = "creat", nullable = false, updatable = false)
    private LocalDateTime fechaIngreso = LocalDateTime.now();

    @Column(name = "raza", length = 50)
    private String raza;

    @Column(name = "dias_nacido")
    private Integer diasNacido;
}