package com.granja.dos.huevitos.service;

import com.granja.dos.huevitos.dto.AnalisisGalponDTO;
import com.granja.dos.huevitos.dto.ProduccionRegistroDTO.*;
import com.granja.dos.huevitos.exception.BadRequestException;
import com.granja.dos.huevitos.models.infrastructure.Galpon;
import com.granja.dos.huevitos.models.infrastructure.LoteGalpon;
import com.granja.dos.huevitos.models.produccion.*;
import com.granja.dos.huevitos.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProduccionRegistroService {
    private final TipoHuevoRepository tipos;
    private final ProduccionGalponRepository producciones;
    private final DetalleProduccionRepository detalles;
    private final ProduccionSectorRepository sectores;
    private final GalponRepository galpones;
    private final LoteGalponRepository lotes;

    public List<TipoResponse> listarTipos() {
        return tipos.findAll().stream().sorted(Comparator.comparing(TipoHuevo::getNombre))
                .map(t -> new TipoResponse(t.getIdTipo(), t.getNombre(), t.getDescripcion())).toList();
    }

    @Transactional
    public TipoResponse crearTipo(TipoRequest request) {
        String nombre = request.nombre().trim();
        if (nombre.isEmpty()) throw new BadRequestException("Ingrese el nombre del tipo de huevo.");
        if (tipos.findAll().stream().anyMatch(t -> t.getNombre().equalsIgnoreCase(nombre)))
            throw new BadRequestException("Ya existe un tipo de huevo con ese nombre.");
        TipoHuevo tipo = new TipoHuevo();
        tipo.setNombre(nombre);
        tipo.setDescripcion(request.descripcion());
        tipo = tipos.save(tipo);
        return new TipoResponse(tipo.getIdTipo(), tipo.getNombre(), tipo.getDescripcion());
    }

    @Transactional
    public TipoResponse editarTipo(Integer id, TipoRequest request) {
        TipoHuevo tipo = tipos.findById(id)
                .orElseThrow(() -> new BadRequestException("El tipo de huevo no existe."));
        String nombre = request.nombre().trim();
        if (nombre.isEmpty()) throw new BadRequestException("Ingrese el nombre del tipo de huevo.");
        if (tipos.findAll().stream().anyMatch(t -> !t.getIdTipo().equals(id)
                && t.getNombre().equalsIgnoreCase(nombre)))
            throw new BadRequestException("Ya existe un tipo de huevo con ese nombre.");
        tipo.setNombre(nombre);
        tipo.setDescripcion(request.descripcion());
        tipo = tipos.save(tipo);
        return new TipoResponse(tipo.getIdTipo(), tipo.getNombre(), tipo.getDescripcion());
    }

    @Transactional
    public RegistroResponse registrar(RegistroRequest request) {
        if (request.fecha().isAfter(LocalDate.now(ZoneId.of("America/Lima"))))
            throw new BadRequestException("La fecha no puede ser futura.");
        Galpon galpon = producciones.bloquearGalpon(request.galponId())
                .orElseThrow(() -> new BadRequestException("El galpón no existe."));
        if (!"Activo".equalsIgnoreCase(galpon.getEstado()))
            throw new BadRequestException("Seleccione un galpón activo.");
        if (galpon.getSector() == null) throw new BadRequestException("El galpón no tiene sector.");
        if (producciones.existsByGalpon_IdGalponAndFecha(request.galponId(), request.fecha()))
            throw new BadRequestException("Ya existe un registro para ese galpón y fecha.");

        Set<Integer> ids = new HashSet<>();
        List<DetalleProduccion> items = new ArrayList<>();
        long total = 0;
        for (DetalleRequest entrada : request.detalles()) {
            if (!ids.add(entrada.tipoHuevoId())) throw new BadRequestException("Hay tipos de huevo repetidos.");
            if (entrada.cantidad() < 0) throw new BadRequestException("Las cantidades no pueden ser negativas.");
            TipoHuevo tipo = tipos.findById(entrada.tipoHuevoId())
                    .orElseThrow(() -> new BadRequestException("El tipo de huevo no existe."));
            DetalleProduccion detalle = new DetalleProduccion();
            detalle.setTipoHuevo(tipo);
            detalle.setCantidad(entrada.cantidad());
            items.add(detalle);
            total += entrada.cantidad();
        }
        if (total > Integer.MAX_VALUE) throw new BadRequestException("El total supera el límite permitido.");
        ProduccionGalpon produccion = new ProduccionGalpon();
        produccion.setFecha(request.fecha());
        produccion.setGalpon(galpon);
        produccion.setObservacion(request.observacion());
        produccion.setTotalHuevos((int) total);
        producciones.save(produccion);
        items.forEach(d -> d.setProduccion(produccion));
        detalles.saveAll(items);
        produccion.setDetalles(items);

        // El esquema existente vincula cada resumen con una producción de galpón.
        // La consulta consolida estos aportes por sector y fecha.
        ProduccionSector resumen = new ProduccionSector();
        resumen.setProduccion(produccion);
        resumen.setFecha(request.fecha());
        resumen.setSector(galpon.getSector());
        resumen.setTotalHuevos((int) total);
        sectores.save(resumen);
        return respuesta(produccion);
    }

    public List<RegistroResponse> listar() {
        return producciones.findAll().stream()
                .sorted(Comparator.comparing(ProduccionGalpon::getFecha).reversed()
                        .thenComparing(ProduccionGalpon::getIdProduccion, Comparator.reverseOrder()))
                .map(this::respuesta).toList();
    }

    public List<SectorResponse> resumen() {
        record Clave(LocalDate fecha, Integer id, String nombre) {}
        return sectores.findAll().stream().collect(Collectors.groupingBy(
                p -> new Clave(p.getFecha(), p.getSector().getIdSector(), p.getSector().getNombre()),
                Collectors.summingLong(ProduccionSector::getTotalHuevos)))
                .entrySet().stream().map(e -> new SectorResponse(e.getKey().fecha(), e.getKey().id(),
                        e.getKey().nombre(), e.getValue()))
                .sorted(Comparator.comparing(SectorResponse::fecha).reversed().thenComparing(SectorResponse::sector))
                .toList();
    }

    public AnalisisGalponDTO analisis(Integer id) {
        return analisis(id, null, false);
    }

    public AnalisisGalponDTO analisis(Integer id, LocalDate fecha, boolean masProductivo) {
        Galpon galpon = galpones.findById(id).orElseThrow(() -> new BadRequestException("El galpón no existe."));
        List<ProduccionGalpon> registros = producciones.findByGalpon_IdGalpon(id);
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Lima"));
        int aves = lotes.findByGalpon_IdGalpon(id).stream().mapToInt(LoteGalpon::getCantidadAves).sum();
        Map<LocalDate, Long> porDia = registros.stream().collect(Collectors.groupingBy(
                ProduccionGalpon::getFecha, Collectors.summingLong(ProduccionGalpon::getTotalHuevos)));
        LocalDate fechaConsultada = fecha == null ? hoy.minusDays(1) : fecha;
        if (masProductivo) {
            fechaConsultada = porDia.entrySet().stream()
                    .max(Comparator.<Map.Entry<LocalDate, Long>>comparingLong(Map.Entry::getValue)
                            .thenComparing(Map.Entry::getKey))
                    .map(Map.Entry::getKey).orElse(null);
        }
        double promedio = porDia.values().stream().mapToLong(Long::longValue).average().orElse(0);
        double mensual = porDia.entrySet().stream().filter(e -> e.getKey().getYear() == hoy.getYear()
                        && e.getKey().getMonth() == hoy.getMonth())
                .mapToLong(Map.Entry::getValue).average().orElse(0);
        AnalisisGalponDTO dto = new AnalisisGalponDTO();
        dto.setIdGalpon(id);
        dto.setNombreGalpon(galpon.getNombre());
        dto.setNombreSector(galpon.getSector().getNombre());
        dto.setCantidadGallinas(aves);
        dto.setProduccionAyer(Math.toIntExact(porDia.getOrDefault(hoy.minusDays(1), 0L)));
        dto.setFechaConsultada(fechaConsultada);
        dto.setProduccionFecha(fechaConsultada == null ? 0L : porDia.getOrDefault(fechaConsultada, 0L));
        dto.setPromedioProduccionDiaria(promedio);
        dto.setPromedioProduccionMes(mensual);
        dto.setEstadoProduccion(registros.isEmpty() ? "Sin registros" : aves == 0 ? "Sin aves asignadas"
                : promedio >= aves * 0.8 ? "Óptima" : promedio >= aves * 0.5 ? "Regular" : "Deficiente");
        return dto;
    }

    private RegistroResponse respuesta(ProduccionGalpon p) {
        return new RegistroResponse(p.getIdProduccion(), p.getFecha(), p.getGalpon().getIdGalpon(),
                p.getGalpon().getNombre(), p.getGalpon().getSector().getNombre(), p.getTotalHuevos(),
                p.getObservacion(), p.getDetalles().stream().map(d -> new DetalleResponse(
                        d.getTipoHuevo().getIdTipo(), d.getTipoHuevo().getNombre(), d.getCantidad())).toList());
    }
}
