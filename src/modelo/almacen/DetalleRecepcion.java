package modelo.almacen;

/**
 * Línea de detalle de una RecepcionAlmacen: cuántos huevos de una
 * clasificación (Rojo, Pardo, Jumbo, etc.) llegaron y cuántos de esos
 * llegaron rotos por el traslado desde el galpón.
 * Corresponde a la entidad "DetalleRecepcion" del diagrama UML
 * (reciepcionId -> idRecepcion, tipoHuevold -> idClasificacion).
 */
public class DetalleRecepcion {

    private final Long id;
    private final Long idRecepcion;
    private final ClasificacionHuevo clasificacion;
    private final int cantidad;
    private final int cantidadRota;

    public DetalleRecepcion(Long id, Long idRecepcion, ClasificacionHuevo clasificacion,
                             int cantidad, int cantidadRota) {
        // idRecepcion puede venir null: la RecepcionAlmacen todavía no tiene
        // id propio (se lo asigna la base de datos recién al guardarse).
        if (clasificacion == null) {
            throw new IllegalArgumentException("La clasificación del huevo es obligatoria.");
        }
        if (cantidad < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa.");
        }
        if (cantidadRota < 0) {
            throw new IllegalArgumentException("La cantidad rota no puede ser negativa.");
        }
        if (cantidadRota > cantidad) {
            throw new IllegalArgumentException("La cantidad rota no puede superar la cantidad recibida.");
        }

        this.id = id;
        this.idRecepcion = idRecepcion;
        this.clasificacion = clasificacion;
        this.cantidad = cantidad;
        this.cantidadRota = cantidadRota;
    }

    public Long getId() {
        return id;
    }

    public Long getIdRecepcion() {
        return idRecepcion;
    }

    public ClasificacionHuevo getClasificacion() {
        return clasificacion;
    }

    public int getCantidad() {
        return cantidad;
    }

    public int getCantidadRota() {
        return cantidadRota;
    }

    /** Huevos de esta clasificación que quedaron en buen estado tras el traslado. */
    public int getCantidadBuena() {
        return cantidad - cantidadRota;
    }
}
