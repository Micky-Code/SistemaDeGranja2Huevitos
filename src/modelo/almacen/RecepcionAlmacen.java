package modelo.almacen;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Reconteo en Almacén de la producción que llega desde un galpón
 * (tabla recepcion_almacen). Aquí se registran, por clasificación, las
 * cantidades recibidas y las roturas ocurridas durante el traslado.
 */
public class RecepcionAlmacen {

    private final Long id;
    private final Long idProduccion; // referencia a produccion_galpon (módulo de Producción)
    private final LocalDate fecha;
    private final Long idEmpleado;   // responsable de almacén que hizo el reconteo
    private String observacion;
    private final List<DetalleRecepcion> detalles;

    public RecepcionAlmacen(Long id, Long idProduccion, LocalDate fecha, Long idEmpleado) {
        if (idProduccion == null) {
            throw new IllegalArgumentException("La recepción debe referenciar una producción de galpón.");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de recepción es obligatoria.");
        }
        if (idEmpleado == null) {
            throw new IllegalArgumentException("La recepción debe tener un responsable de almacén.");
        }

        this.id = id;
        this.idProduccion = idProduccion;
        this.fecha = fecha;
        this.idEmpleado = idEmpleado;
        this.observacion = "";
        this.detalles = new ArrayList<>();
    }

    /** Registra cuántos huevos de una clasificación llegaron (y cuántos rotos). */
    public void registrarDetalle(DetalleRecepcion detalle) {
        if (detalle == null) {
            throw new IllegalArgumentException("El detalle a registrar no puede ser nulo.");
        }
        // Si ambos ya tienen id asignado (vinieron de la base de datos), deben coincidir.
        // Antes de guardarse, cualquiera de los dos puede venir en null.
        if (detalle.getIdRecepcion() != null && this.id != null
                && !Objects.equals(detalle.getIdRecepcion(), this.id)) {
            throw new IllegalArgumentException("El detalle no corresponde a esta recepción.");
        }
        this.detalles.add(detalle);
    }

    /** Total de huevos recibidos, sumando todas las clasificaciones. */
    public int getCantidadTotalRecibida() {
        int total = 0;
        for (DetalleRecepcion d : detalles) {
            total += d.getCantidad();
        }
        return total;
    }

    /** Merma total del traslado: huevos rotos, sumando todas las clasificaciones. */
    public int getMermaTraslado() {
        int rotos = 0;
        for (DetalleRecepcion d : detalles) {
            rotos += d.getCantidadRota();
        }
        return rotos;
    }

    /** Porcentaje de merma respecto a lo recibido (0 si aún no hay detalles). */
    public double getPorcentajeMerma() {
        int total = getCantidadTotalRecibida();
        if (total == 0) {
            return 0.0;
        }
        return (getMermaTraslado() * 100.0) / total;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion == null ? "" : observacion;
    }

    public Long getId() {
        return id;
    }

    public Long getIdProduccion() {
        return idProduccion;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public Long getIdEmpleado() {
        return idEmpleado;
    }

    public String getObservacion() {
        return observacion;
    }

    public List<DetalleRecepcion> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }
}
