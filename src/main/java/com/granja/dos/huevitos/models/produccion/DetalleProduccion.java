package com.granja.dos.huevitos.models.produccion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "detalle_produccion", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class DetalleProduccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Integer idDetalle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produccion_id", nullable = false)
    @JsonIgnore
    private ProduccionGalpon produccion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo_huevo_id", nullable = false)
    @JsonIgnore
    private TipoHuevo tipoHuevo;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;
}
