package com.granja.dos.huevitos.models.reportes;

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

/**
 * Línea de un Reporte: una descripción y su valor calculado
 * (ej. "Total recibido" -> "1500", "% Merma" -> "3.2%").
 */
@Entity
@Table(name = "detalle_reporte", schema = "Avicola")
@Getter
@Setter
@NoArgsConstructor
public class DetalleReporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Integer idDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reporte", nullable = false)
    private Reporte reporte;

    @Column(name = "descripcion", length = 200, nullable = false)
    private String descripcion;

    @Column(name = "valor", length = 200)
    private String valor;

    public DetalleReporte(String descripcion, String valor) {
        this.descripcion = descripcion;
        this.valor = valor;
    }
}
