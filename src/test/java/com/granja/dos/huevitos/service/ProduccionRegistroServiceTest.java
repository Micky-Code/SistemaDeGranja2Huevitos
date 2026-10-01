package com.granja.dos.huevitos.service;

import com.granja.dos.huevitos.dto.ProduccionRegistroDTO.*;
import com.granja.dos.huevitos.exception.BadRequestException;
import com.granja.dos.huevitos.models.infrastructure.*;
import com.granja.dos.huevitos.models.produccion.*;
import com.granja.dos.huevitos.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProduccionRegistroServiceTest {
    private final TipoHuevoRepository tipos = mock(TipoHuevoRepository.class);
    private final ProduccionGalponRepository producciones = mock(ProduccionGalponRepository.class);
    private final DetalleProduccionRepository detalles = mock(DetalleProduccionRepository.class);
    private final ProduccionSectorRepository sectores = mock(ProduccionSectorRepository.class);
    private final GalponRepository galpones = mock(GalponRepository.class);
    private final LoteGalponRepository lotes = mock(LoteGalponRepository.class);
    private final ProduccionRegistroService service = new ProduccionRegistroService(tipos, producciones, detalles, sectores, galpones, lotes);
    private final LocalDate fecha = LocalDate.of(2026, 1, 1);
    private Sector sector;

    @BeforeEach
    void preparar() {
        sector = new Sector(); sector.setIdSector(1); sector.setNombre("Norte");
        Galpon galpon = new Galpon(); galpon.setIdGalpon(2); galpon.setNombre("G2"); galpon.setSector(sector);
        when(producciones.bloquearGalpon(2)).thenReturn(Optional.of(galpon));
        for (int id = 1; id <= 2; id++) {
            TipoHuevo tipo = new TipoHuevo(); tipo.setIdTipo(id); tipo.setNombre("Tipo " + id);
            when(tipos.findById(id)).thenReturn(Optional.of(tipo));
        }
        when(producciones.save(any())).thenAnswer(invocation -> {
            ProduccionGalpon p = invocation.getArgument(0); p.setIdProduccion(10); return p;
        });
    }

    private RegistroRequest request(DetalleRequest... items) {
        return new RegistroRequest(fecha, 2, "Recolección", List.of(items));
    }

    @Test
    void guardaTotalDetallesYResumenDelSector() {
        RegistroResponse response = service.registrar(request(new DetalleRequest(1, 20), new DetalleRequest(2, 5)));
        assertEquals(25, response.totalHuevos()); assertEquals(2, response.detalles().size());
        verify(detalles).saveAll(argThat(items -> {
            List<DetalleProduccion> lista = new ArrayList<>(); items.forEach(lista::add);
            return lista.size() == 2 && lista.stream().allMatch(d -> d.getProduccion().getIdProduccion() == 10);
        }));
        verify(sectores).save(argThat(p -> p.getTotalHuevos() == 25 && p.getSector() == sector && p.getFecha().equals(fecha)));
    }

    @Test
    void rechazaRegistroDuplicadoSinGuardar() {
        when(producciones.existsByGalpon_IdGalponAndFecha(2, fecha)).thenReturn(true);
        assertThrows(BadRequestException.class, () -> service.registrar(request(new DetalleRequest(1, 1))));
        verify(producciones, never()).save(any()); verifyNoInteractions(detalles, sectores);
    }

    @Test
    void rechazaTiposRepetidos() {
        assertThrows(BadRequestException.class, () -> service.registrar(request(new DetalleRequest(1, 2), new DetalleRequest(1, 3))));
        verify(producciones, never()).save(any());
    }

    @Test
    void rechazaCantidadNegativaYTotalFueraDeRango() {
        assertThrows(BadRequestException.class, () -> service.registrar(request(new DetalleRequest(1, -1))));
        assertThrows(BadRequestException.class, () -> service.registrar(request(new DetalleRequest(1, Integer.MAX_VALUE), new DetalleRequest(2, 1))));
        verify(producciones, never()).save(any());
    }

    @Test
    void rechazaTipoInexistente() {
        assertThrows(BadRequestException.class, () -> service.registrar(request(new DetalleRequest(99, 1))));
        verify(producciones, never()).save(any());
    }

    @Test
    void consolidaAportesDeVariosGalpones() {
        ProduccionSector a = new ProduccionSector(); a.setSector(sector); a.setFecha(fecha); a.setTotalHuevos(20);
        ProduccionSector b = new ProduccionSector(); b.setSector(sector); b.setFecha(fecha); b.setTotalHuevos(30);
        when(sectores.findAll()).thenReturn(List.of(a, b));
        List<SectorResponse> resumen = service.resumen();
        assertEquals(1, resumen.size()); assertEquals(50L, resumen.getFirst().totalHuevos());
    }
}
