package com.granja.dos.huevitos.models.almacen;

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
 * Línea de detalle de una RecepcionAlmacen: cuántos huevos de una
 * clasificación llegaron y cuántos de esos llegaron rotos por el traslado
 * desde el galpón.
 */
@Entity
@Table(name = "recepcion_almacen_detalle", schema = "Avicola")
@Getter
@Setter
@NoArgsConstructor
public class DetalleRecepcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Integer idDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_recepcion", nullable = false)
    private RecepcionAlmacen recepcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_clasificacion", nullable = false)
    private ClasificacionHuevo clasificacion;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "cantidad_rota", nullable = false)
    private Integer cantidadRota = 0;

    public DetalleRecepcion(ClasificacionHuevo clasificacion, Integer cantidad, Integer cantidadRota) {
        this.clasificacion = clasificacion;
        this.cantidad = cantidad;
        this.cantidadRota = cantidadRota == null ? 0 : cantidadRota;
    }

    /** Huevos de esta clasificación que quedaron en buen estado tras el traslado. */
    public int getCantidadBuena() {
        int total = cantidad == null ? 0 : cantidad;
        int rotos = cantidadRota == null ? 0 : cantidadRota;
        return total - rotos;
    }
}
