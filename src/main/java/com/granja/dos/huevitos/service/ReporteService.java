package com.granja.dos.huevitos.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.granja.dos.huevitos.dto.DetalleReporteResponse;
import com.granja.dos.huevitos.dto.ReporteResponse;
import com.granja.dos.huevitos.exception.BadRequestException;
import com.granja.dos.huevitos.models.almacen.DetalleRecepcion;
import com.granja.dos.huevitos.models.almacen.RecepcionAlmacen;
import com.granja.dos.huevitos.models.reportes.Reporte;
import com.granja.dos.huevitos.models.segurity.Usuario;
import com.granja.dos.huevitos.repository.RecepcionAlmacenRepository;
import com.granja.dos.huevitos.repository.ReporteRepository;
import com.granja.dos.huevitos.repository.UsuarioRepository;

/**
 * Lógica de negocio del módulo "9. Reportes" (obligatorio para todos los
 * módulos). Por ahora genera el reporte de mermas de Almacén; nuevos tipos de
 * reporte (Producción, Sanidad, etc.) se agregan con su propio método
 * "generarReporteX" siguiendo el mismo patrón.
 */
@Service
public class ReporteService {

    private final ReporteRepository reportes;
    private final RecepcionAlmacenRepository recepciones;
    private final UsuarioRepository usuarios;

    public ReporteService(ReporteRepository reportes, RecepcionAlmacenRepository recepciones,
            UsuarioRepository usuarios) {
        this.reportes = reportes;
        this.recepciones = recepciones;
        this.usuarios = usuarios;
    }

    @Transactional
    public ReporteResponse generarReporteMermasAlmacen(String username) {
        Usuario usuario = usuarios.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("No se encontró el usuario que genera el reporte."));

        List<RecepcionAlmacen> todas = recepciones.findAllByOrderByFechaDesc();

        Reporte reporte = new Reporte("ALMACEN_MERMAS", usuario, "cantidadRecepciones=" + todas.size());

        int totalRecibido = 0;
        int totalRoto = 0;
        Map<String, Integer> recibidoPorClasificacion = new HashMap<>();
        Map<String, Integer> rotoPorClasificacion = new HashMap<>();

        for (RecepcionAlmacen recepcion : todas) {
            totalRecibido += recepcion.getCantidadTotalRecibida();
            totalRoto += recepcion.getMermaTraslado();

            for (DetalleRecepcion detalle : recepcion.getDetalles()) {
                String nombre = detalle.getClasificacion().getNombre();
                recibidoPorClasificacion.merge(nombre, detalle.getCantidad(), Integer::sum);
                rotoPorClasificacion.merge(nombre, detalle.getCantidadRota(), Integer::sum);
            }
        }

        reporte.agregarDetalle("Recepciones procesadas", String.valueOf(todas.size()));
        reporte.agregarDetalle("Total recibido", String.valueOf(totalRecibido));
        reporte.agregarDetalle("Total roto en traslado", String.valueOf(totalRoto));

        double porcentaje = totalRecibido == 0 ? 0.0 : (totalRoto * 100.0) / totalRecibido;
        reporte.agregarDetalle("Porcentaje de merma", String.format("%.2f%%", porcentaje));

        for (Map.Entry<String, Integer> entry : recibidoPorClasificacion.entrySet()) {
            String clasificacion = entry.getKey();
            int recibido = entry.getValue();
            int roto = rotoPorClasificacion.getOrDefault(clasificacion, 0);
            reporte.agregarDetalle("Clasificación " + clasificacion + " (recibido/roto)", recibido + " / " + roto);
        }

        return toResponse(reportes.save(reporte));
    }

    @Transactional(readOnly = true)
    public List<ReporteResponse> listarReportes() {
        return reportes.findAllByOrderByFechaGeneracionDesc().stream().map(this::toResponse).toList();
    }

    private ReporteResponse toResponse(Reporte reporte) {
        List<DetalleReporteResponse> detalles = reporte.getDetalles().stream()
                .map(d -> new DetalleReporteResponse(d.getDescripcion(), d.getValor()))
                .toList();

        return new ReporteResponse(
                reporte.getIdReporte(),
                reporte.getTipo(),
                reporte.getFechaGeneracion(),
                reporte.getUsuario().getUsername(),
                reporte.getParametros(),
                detalles);
    }
}
