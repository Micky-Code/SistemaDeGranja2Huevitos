package sistema.reportes;

/**
 * Línea de un Reporte: una descripción y su valor calculado
 * (ej. "Total recibido" -> "1500", "% Merma" -> "3.2").
 * Se guarda como texto para que un mismo Reporte pueda mezclar líneas
 * numéricas, porcentajes o texto libre sin necesitar una tabla por tipo.
 */
public class DetalleReporte {

    private final Long id;
    private final Long idReporte;
    private final String descripcion;
    private final String valor;

    public DetalleReporte(Long id, Long idReporte, String descripcion, String valor) {
        // idReporte puede venir null: el Reporte todavía no tiene id propio
        // (se lo asigna la base de datos recién al guardarse).
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción del detalle es obligatoria.");
        }
        this.id = id;
        this.idReporte = idReporte;
        this.descripcion = descripcion;
        this.valor = valor == null ? "" : valor;
    }

    public Long getId() {
        return id;
    }

    public Long getIdReporte() {
        return idReporte;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getValor() {
        return valor;
    }

    @Override
    public String toString() {
        return descripcion + ": " + valor;
    }
}
