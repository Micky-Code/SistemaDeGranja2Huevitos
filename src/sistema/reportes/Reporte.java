package sistema.reportes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Cabecera genérica de un reporte generado en el sistema (módulo
 * "9. Reportes", obligatorio para todos los módulos: Producción,
 * Sanidad, Alimentación, Almacén, etc.). Un Reporte agrupa varias
 * líneas (DetalleReporte) con los valores calculados, y opcionalmente
 * queda enlazado a un archivo exportado (PDF/CSV/Excel).
 */
public class Reporte {

    private final Long id;
    private final String tipo;
    private final LocalDateTime fechaGeneracion;
    private final Long idUsuario;
    private final String parametros;
    private String archivo;
    private final List<DetalleReporte> detalles;

    public Reporte(Long id, String tipo, Long idUsuario, String parametros) {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de reporte es obligatorio (ej. ALMACEN, PRODUCCION, SANIDAD).");
        }
        if (idUsuario == null) {
            throw new IllegalArgumentException("El reporte debe indicar qué usuario lo generó.");
        }

        this.id = id;
        this.tipo = tipo.trim().toUpperCase();
        this.idUsuario = idUsuario;
        this.parametros = parametros == null ? "" : parametros;
        this.fechaGeneracion = LocalDateTime.now();
        this.archivo = null;
        this.detalles = new ArrayList<>();
    }

    /** Agrega una línea calculada al reporte (ej. "Total recibido" -> "1500"). */
    public void agregarDetalle(String descripcion, String valor) {
        this.detalles.add(new DetalleReporte(null, this.id, descripcion, valor));
    }

    /** Marca el reporte como exportado, guardando la ruta del archivo generado. */
    public void marcarExportado(String rutaArchivo) {
        if (rutaArchivo == null || rutaArchivo.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta del archivo exportado no puede estar vacía.");
        }
        this.archivo = rutaArchivo;
    }

    public boolean isExportado() {
        return archivo != null;
    }

    public Long getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public String getParametros() {
        return parametros;
    }

    public String getArchivo() {
        return archivo;
    }

    public List<DetalleReporte> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }
}
