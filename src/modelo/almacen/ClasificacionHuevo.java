package modelo.almacen;

import java.util.Objects;

/**
 * Catálogo de clasificación de huevos usado en Almacén (tabla
 * clasificaciones_huevo en la base de datos). Corresponde a la entidad
 * "TipoHuevo" del diagrama UML, renombrada aquí a ClasificacionHuevo para
 * no chocar con el enum modelo.paquete.TipoHuevo que ya usa el módulo de
 * Producción (Paquete). Si el equipo prefiere unificarlos, este es el
 * lugar para hacerlo.
 */
public class ClasificacionHuevo {

    private final Long id;
    private final String nombre;
    private final String descripcion;

    public ClasificacionHuevo(Long id, String nombre, String descripcion) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la clasificación no puede estar vacío.");
        }
        this.id = id;
        this.nombre = nombre.trim().toUpperCase();
        this.descripcion = descripcion == null ? "" : descripcion;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClasificacionHuevo)) return false;
        ClasificacionHuevo that = (ClasificacionHuevo) o;
        return nombre.equals(that.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
