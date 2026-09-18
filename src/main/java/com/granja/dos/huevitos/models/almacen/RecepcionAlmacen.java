package com.granja.dos.huevitos.models.almacen;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reconteo en Almacén de la producción que llega desde un galpón. Aquí se
 * registran, por clasificación, las cantidades recibidas y las roturas
 * ocurridas durante el traslado (módulo "7. Almacén y Clasificación Final").
 *
 * id_produccion e id_empleado quedan como referencias simples (Integer)
 * porque las entidades ProduccionGalpon y Empleado todavía no existen en
 * el proyecto (son de otros módulos en desarrollo). Cuando existan, se
 * pueden convertir a @ManyToOne.
 */
@Entity
@Table(name = "recepcion_almacen", schema = "Avicola")
@Getter
@Setter
@NoArgsConstructor
public class RecepcionAlmacen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recepcion")
    private Integer idRecepcion;

    @Column(name = "id_produccion", nullable = false)
    private Integer idProduccion;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "id_empleado", nullable = false)
    private Integer idEmpleado;

    @Column(name = "observacion", length = 255)
    private String observacion;

    @Column(name = "creat", nullable = false)
    private LocalDateTime creat = LocalDateTime.now();

    @OneToMany(mappedBy = "recepcion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleRecepcion> detalles = new ArrayList<>();

    /** Mantiene la relación bidireccional al agregar un detalle. */
    public void agregarDetalle(DetalleRecepcion detalle) {
        detalle.setRecepcion(this);
        this.detalles.add(detalle);
    }

    /** Total de huevos recibidos, sumando todas las clasificaciones. */
    public int getCantidadTotalRecibida() {
        return detalles.stream().mapToInt(d -> nvl(d.getCantidad())).sum();
    }

    /** Merma total del traslado: huevos rotos, sumando todas las clasificaciones. */
    public int getMermaTraslado() {
        return detalles.stream().mapToInt(d -> nvl(d.getCantidadRota())).sum();
    }

    /** Porcentaje de merma respecto a lo recibido (0 si aún no hay detalles). */
    public double getPorcentajeMerma() {
        int total = getCantidadTotalRecibida();
        return total == 0 ? 0.0 : (getMermaTraslado() * 100.0) / total;
    }

    private static int nvl(Integer valor) {
        return valor == null ? 0 : valor;
    }
}
