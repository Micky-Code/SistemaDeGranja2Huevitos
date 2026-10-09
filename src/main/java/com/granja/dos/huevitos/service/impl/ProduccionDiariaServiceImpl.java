package com.granja.dos.huevitos.service.impl;

import com.granja.dos.huevitos.dto.ProduccionDiariaRequestDTO;
import com.granja.dos.huevitos.dto.ProduccionDiariaResponseDTO;
import com.granja.dos.huevitos.models.infrastructure.LoteGalpon;
import com.granja.dos.huevitos.models.produccion.ProduccionDiaria;
import com.granja.dos.huevitos.repository.LoteGalponRepository;
import com.granja.dos.huevitos.repository.ProduccionDiariaRepository;
import com.granja.dos.huevitos.service.ProduccionDiariaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProduccionDiariaServiceImpl implements ProduccionDiariaService {

    private final ProduccionDiariaRepository produccionDiariaRepository;
    private final LoteGalponRepository loteGalponRepository;

    @Override
    @Transactional
    public ProduccionDiariaResponseDTO registrarProduccion(ProduccionDiariaRequestDTO requestDTO) {
        LoteGalpon loteGalpon = loteGalponRepository.findById(requestDTO.getLoteGalponId())
                .orElseThrow(() -> new RuntimeException("LoteGalpon no encontrado"));

        if (produccionDiariaRepository.existsByLoteGalpon_IdLoteGalponAndFecha(requestDTO.getLoteGalponId(), requestDTO.getFecha())) {
            throw new IllegalArgumentException("Error: Ya se ha registrado la producción diaria para este galpón en la fecha seleccionada.");
        }

        ProduccionDiaria produccionDiaria = ProduccionDiaria.builder()
                .fecha(requestDTO.getFecha())
                .cantidadHuevosBuenos(requestDTO.getCantidadHuevosBuenos())
                .cantidadHuevosRotos(requestDTO.getCantidadHuevosRotos())
                .cantidadHuevosSucios(requestDTO.getCantidadHuevosSucios())
                .observaciones(requestDTO.getObservaciones())
                .loteGalpon(loteGalpon)
                .build();

        ProduccionDiaria savedProduccion = produccionDiariaRepository.save(produccionDiaria);
        return mapToDTO(savedProduccion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProduccionDiariaResponseDTO> listarProducciones() {
        return produccionDiariaRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProduccionDiariaResponseDTO obtenerProduccionPorId(Long id) {
        ProduccionDiaria produccion = produccionDiariaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producción no encontrada"));
        return mapToDTO(produccion);
    }

    @Override
    @Transactional(readOnly = true)
    public com.granja.dos.huevitos.dto.AnalisisGalponDTO obtenerAnalisisGalpon(Integer idGalpon) {
        List<LoteGalpon> lotes = loteGalponRepository.findByGalpon_IdGalpon(idGalpon);
        if (lotes.isEmpty()) {
            throw new RuntimeException("El galpón no tiene lotes asignados");
        }
        
        com.granja.dos.huevitos.models.infrastructure.Galpon galpon = lotes.get(0).getGalpon();
        List<ProduccionDiaria> producciones = produccionDiariaRepository.findByLoteGalponIn(lotes);
        
        int cantidadGallinas = lotes.stream().mapToInt(LoteGalpon::getCantidadAves).sum();
        
        java.time.LocalDate hoy = java.time.LocalDate.now();
        java.time.LocalDate ayer = hoy.minusDays(1);
        
        int produccionAyer = producciones.stream()
                .filter(p -> p.getFecha().equals(ayer))
                .mapToInt(ProduccionDiaria::getCantidadHuevosBuenos)
                .sum();
                
        double promedioDiario = producciones.stream()
                .mapToInt(ProduccionDiaria::getCantidadHuevosBuenos)
                .average()
                .orElse(0.0);
                
        double promedioMes = producciones.stream()
                .filter(p -> p.getFecha().getMonth() == hoy.getMonth() && p.getFecha().getYear() == hoy.getYear())
                .mapToInt(ProduccionDiaria::getCantidadHuevosBuenos)
                .average()
                .orElse(0.0);

        String estadoProduccion = "Deficiente (Esperado 1 huevo/gallina)";
        if (promedioDiario >= (cantidadGallinas * 0.8)) { // Tolerancia 80%
            estadoProduccion = "Óptima";
        } else if (promedioDiario >= (cantidadGallinas * 0.5)) {
            estadoProduccion = "Regular";
        }

        com.granja.dos.huevitos.dto.AnalisisGalponDTO dto = new com.granja.dos.huevitos.dto.AnalisisGalponDTO();
        dto.setIdGalpon(idGalpon);
        dto.setNombreGalpon(galpon.getNombre());
        dto.setNombreSector(galpon.getSector().getNombre());
        dto.setCantidadGallinas(cantidadGallinas);
        dto.setProduccionAyer(produccionAyer);
        dto.setPromedioProduccionDiaria(promedioDiario);
        dto.setPromedioProduccionMes(promedioMes);
        dto.setEstadoProduccion(estadoProduccion);
        
        return dto;
    }

    private ProduccionDiariaResponseDTO mapToDTO(ProduccionDiaria produccionDiaria) {
        ProduccionDiariaResponseDTO dto = new ProduccionDiariaResponseDTO();
        dto.setId(produccionDiaria.getId());
        dto.setFecha(produccionDiaria.getFecha());
        dto.setCantidadHuevosBuenos(produccionDiaria.getCantidadHuevosBuenos());
        dto.setCantidadHuevosRotos(produccionDiaria.getCantidadHuevosRotos());
        dto.setCantidadHuevosSucios(produccionDiaria.getCantidadHuevosSucios());
        dto.setObservaciones(produccionDiaria.getObservaciones());
        dto.setLoteGalponId(produccionDiaria.getLoteGalpon().getIdLoteGalpon());
        return dto;
    }
}
