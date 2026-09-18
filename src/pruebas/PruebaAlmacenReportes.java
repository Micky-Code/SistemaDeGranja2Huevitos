package pruebas;

import modelo.almacen.ClasificacionHuevo;
import modelo.almacen.DetalleRecepcion;
import modelo.almacen.RecepcionAlmacen;
import sistema.reportes.Reporte;
import sistema.reportes.GeneradorReporteAlmacen;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de PRUEBA (no forma parte del modelo final) para verificar a
 * mano, desde NetBeans, que las clases de Almacen y Reportes compilan
 * y funcionan como se espera. Ejecutar con: click derecho sobre este
 * archivo -> "Run File" (o Shift+F6).
 *
 * Borrar o mover fuera de src/ cuando ya no se necesite.
 */
public class PruebaAlmacenReportes {

    public static void main(String[] args) {
        // 1. Catálogo de clasificaciones (normalmente vendría de la BD)
        ClasificacionHuevo rojo = new ClasificacionHuevo(1L, "ROJO", "Huevo rojo/pardo oscuro");
        ClasificacionHuevo jumbo = new ClasificacionHuevo(2L, "JUMBO", "Huevo extra grande");

        // 2. Simulamos dos recepciones de almacén
        List<RecepcionAlmacen> recepciones = new ArrayList<>();

        RecepcionAlmacen recepcion1 = new RecepcionAlmacen(1L, /*idProduccion*/ 10L, LocalDate.now(), /*idEmpleado*/ 5L);
        recepcion1.registrarDetalle(new DetalleRecepcion(1L, 1L, rojo, 500, 15));
        recepcion1.registrarDetalle(new DetalleRecepcion(2L, 1L, jumbo, 200, 3));
        recepciones.add(recepcion1);

        RecepcionAlmacen recepcion2 = new RecepcionAlmacen(2L, 11L, LocalDate.now(), 5L);
        recepcion2.registrarDetalle(new DetalleRecepcion(3L, 2L, rojo, 480, 5));
        recepciones.add(recepcion2);

        // 3. Verificamos los cálculos de una recepción individual
        System.out.println("=== Recepción 1 ===");
        System.out.println("Total recibido: " + recepcion1.getCantidadTotalRecibida());
        System.out.println("Merma traslado: " + recepcion1.getMermaTraslado());
        System.out.printf("Porcentaje merma: %.2f%%%n", recepcion1.getPorcentajeMerma());

        // 4. Generamos el reporte consolidado (backend: Almacén -> Reportes)
        Reporte reporte = GeneradorReporteAlmacen.generarReporteMermas(recepciones, /*idUsuario*/ 1L);

        System.out.println("\n=== Reporte generado ===");
        System.out.println("Tipo: " + reporte.getTipo());
        System.out.println("Fecha generación: " + reporte.getFechaGeneracion());
        reporte.getDetalles().forEach(System.out::println);
    }
}
