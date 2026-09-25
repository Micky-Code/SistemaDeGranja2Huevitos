package com.granja.dos.huevitos.models.infrastructure;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lote_galpon", schema = "avicola")
@Getter
@Setter
@NoArgsConstructor
public class LoteGalpon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lote_galpon")
    private Integer idLoteGalpon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "galpon_id", nullable = false)
    private Galpon galpon;

    @Column(name = "cantidad_aves", nullable = false)
    private Integer cantidadAves;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;
}
