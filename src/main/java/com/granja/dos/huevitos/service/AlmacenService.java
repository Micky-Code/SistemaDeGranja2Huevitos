package com.granja.dos.huevitos.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.granja.dos.huevitos.dto.ClasificacionHuevoResponse;
import com.granja.dos.huevitos.dto.DetalleRecepcionRequest;
import com.granja.dos.huevitos.dto.DetalleRecepcionResponse;
import com.granja.dos.huevitos.dto.RecepcionAlmacenRequest;
import com.granja.dos.huevitos.dto.RecepcionAlmacenResponse;
import com.granja.dos.huevitos.exception.BadRequestException;
import com.granja.dos.huevitos.models.almacen.ClasificacionHuevo;
import com.granja.dos.huevitos.models.almacen.DetalleRecepcion;
import com.granja.dos.huevitos.models.almacen.RecepcionAlmacen;
import com.granja.dos.huevitos.repository.ClasificacionHuevoRepository;
import com.granja.dos.huevitos.repository.RecepcionAlmacenRepository;

/**
 * Lógica de negocio del módulo "7. Almacén y Clasificación Final": registrar
 * el reconteo que llega desde los galpones y consultar lo ya recibido.
 */
@Service
public class AlmacenService {

    private final RecepcionAlmacenRepository recepciones;
    private final ClasificacionHuevoRepository clasificaciones;

    public AlmacenService(RecepcionAlmacenRepository recepciones, ClasificacionHuevoRepository clasificaciones) {
        this.recepciones = recepciones;
        this.clasificaciones = clasificaciones;
    }

    @Transactional(readOnly = true)
    public List<ClasificacionHuevoResponse> listarClasificaciones() {
        return clasificaciones.findAll().stream()
                .map(c -> new ClasificacionHuevoResponse(c.getIdClasificacion(), c.getNombre(), c.getDescripcion()))
                .toList();
    }

    @Transactional
    public RecepcionAlmacenResponse registrarRecepcion(RecepcionAlmacenRequest request) {
        RecepcionAlmacen recepcion = new RecepcionAlmacen();
        recepcion.setIdProduccion(request.idProduccion());
        recepcion.setFecha(request.fecha());
        recepcion.setIdEmpleado(request.idEmpleado());
        recepcion.setObservacion(request.observacion());

        for (DetalleRecepcionRequest detalleRequest : request.detalles()) {
            ClasificacionHuevo clasificacion = clasificaciones.findById(detalleRequest.idClasificacion())
                    .orElseThrow(() -> new BadRequestException(
                            "No existe la clasificación de huevo con id " + detalleRequest.idClasificacion() + "."));

            if (detalleRequest.cantidadRota() > detalleRequest.cantidad()) {
                throw new BadRequestException(
                        "La cantidad rota no puede superar la cantidad recibida (clasificación "
                                + clasificacion.getNombre() + ").");
            }

            recepcion.agregarDetalle(
                    new DetalleRecepcion(clasificacion, detalleRequest.cantidad(), detalleRequest.cantidadRota()));
        }

        return toResponse(recepciones.save(recepcion));
    }

    @Transactional(readOnly = true)
    public List<RecepcionAlmacenResponse> listarRecepciones() {
        return recepciones.findAllByOrderByFechaDesc().stream().map(this::toResponse).toList();
    }

    private RecepcionAlmacenResponse toResponse(RecepcionAlmacen recepcion) {
        List<DetalleRecepcionResponse> detalles = recepcion.getDetalles().stream()
                .map(d -> new DetalleRecepcionResponse(
                        d.getIdDetalle(),
                        d.getClasificacion().getIdClasificacion(),
                        d.getClasificacion().getNombre(),
                        d.getCantidad(),
                        d.getCantidadRota(),
                        d.getCantidadBuena()))
                .toList();

        return new RecepcionAlmacenResponse(
                recepcion.getIdRecepcion(),
                recepcion.getIdProduccion(),
                recepcion.getFecha(),
                recepcion.getIdEmpleado(),
                recepcion.getObservacion(),
                recepcion.getCantidadTotalRecibida(),
                recepcion.getMermaTraslado(),
                recepcion.getPorcentajeMerma(),
                detalles);
    }
}
