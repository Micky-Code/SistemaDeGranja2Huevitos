package com.granja.dos.huevitos.models.produccion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.granja.dos.huevitos.models.infrastructure.Galpon;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "produccion_galpon", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class ProduccionGalpon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produccion")
    private Integer idProduccion;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "galpon_id", nullable = false)
    @JsonIgnore
    private Galpon galpon;

    @Column(name = "total_huevos", nullable = false)
    private Integer totalHuevos = 0;

    @Column(name = "observacion", columnDefinition = "text")
    private String observacion;

    @OneToMany(mappedBy = "produccion", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<DetalleProduccion> detalles = new ArrayList<>();

    @OneToMany(mappedBy = "produccion", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ProduccionSector> produccionesSector = new ArrayList<>();
}
