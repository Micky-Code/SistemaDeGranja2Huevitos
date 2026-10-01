package com.granja.dos.huevitos.models.produccion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.granja.dos.huevitos.models.infrastructure.Sector;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "produccion_sector", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class ProduccionSector {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produccion_sector")
    private Integer idProduccionSector;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produccion_id", nullable = false)
    @JsonIgnore
    private ProduccionGalpon produccion;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sector_id", nullable = false)
    @JsonIgnore
    private Sector sector;

    @Column(name = "total_huevos", nullable = false)
    private Integer totalHuevos = 0;
}
