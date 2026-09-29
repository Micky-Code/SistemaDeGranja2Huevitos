package com.granja.dos.huevitos.models.produccion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.granja.dos.huevitos.models.infrastructure.LoteGalpon;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "produccion_diaria", schema = "avicola")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProduccionDiaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "cantidad_huevos_buenos", nullable = false)
    private Integer cantidadHuevosBuenos;

    @Column(name = "cantidad_huevos_rotos", nullable = false)
    private Integer cantidadHuevosRotos;

    @Column(name = "cantidad_huevos_sucios", nullable = false)
    private Integer cantidadHuevosSucios;

    @Column(length = 500)
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_galpon_id", nullable = false)
    @JsonIgnore
    private LoteGalpon loteGalpon;
}
