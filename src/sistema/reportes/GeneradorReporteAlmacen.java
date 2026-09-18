package sistema.reportes;

import modelo.almacen.ClasificacionHuevo;
import modelo.almacen.DetalleRecepcion;
import modelo.almacen.RecepcionAlmacen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lógica de negocio (backend) que arma un Reporte a partir de las
 * recepciones registradas en Almacén: totales por clasificación,
 * merma total y porcentaje de merma. Esta clase es la que conecta el
 * módulo 7 (Almacén) con el módulo 9 (Reportes) del diagrama.
 */
public class GeneradorReporteAlmacen {

    private GeneradorReporteAlmacen() {
        // clase de utilidades: no se instancia
    }

    /**
     * Genera un reporte de mermas de traslado a partir de una lista de
     * recepciones (por ejemplo, todas las recepciones de un día o de una
     * semana).
     */
    public static Reporte generarReporteMermas(List<RecepcionAlmacen> recepciones, Long idUsuario) {
        if (recepciones == null) {
            throw new IllegalArgumentException("La lista de recepciones no puede ser nula.");
        }

        Reporte reporte = new Reporte(null, "ALMACEN_MERMAS", idUsuario,
                "cantidadRecepciones=" + recepciones.size());

        int totalRecibido = 0;
        int totalRoto = 0;
        Map<String, Integer> recibidoPorClasificacion = new HashMap<>();
        Map<String, Integer> rotoPorClasificacion = new HashMap<>();

        for (RecepcionAlmacen recepcion : recepciones) {
            totalRecibido += recepcion.getCantidadTotalRecibida();
            totalRoto += recepcion.getMermaTraslado();

            for (DetalleRecepcion detalle : recepcion.getDetalles()) {
                String nombre = detalle.getClasificacion().getNombre();
                recibidoPorClasificacion.merge(nombre, detalle.getCantidad(), Integer::sum);
                rotoPorClasificacion.merge(nombre, detalle.getCantidadRota(), Integer::sum);
            }
        }

        reporte.agregarDetalle("Recepciones procesadas", String.valueOf(recepciones.size()));
        reporte.agregarDetalle("Total recibido", String.valueOf(totalRecibido));
        reporte.agregarDetalle("Total roto en traslado", String.valueOf(totalRoto));

        double porcentaje = totalRecibido == 0 ? 0.0 : (totalRoto * 100.0) / totalRecibido;
        reporte.agregarDetalle("Porcentaje de merma", String.format("%.2f%%", porcentaje));

        for (Map.Entry<String, Integer> entry : recibidoPorClasificacion.entrySet()) {
            String clasificacion = entry.getKey();
            int recibido = entry.getValue();
            int roto = rotoPorClasificacion.getOrDefault(clasificacion, 0);
            reporte.agregarDetalle(
                    "Clasificacion " + clasificacion + " (recibido/roto)",
                    recibido + " / " + roto
            );
        }

        return reporte;
    }
}
