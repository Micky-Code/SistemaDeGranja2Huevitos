package com.granja.dos.huevitos.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.granja.dos.huevitos.dto.GalponResponseDTO;
import com.granja.dos.huevitos.models.infrastructure.Galpon;
import com.granja.dos.huevitos.models.infrastructure.Sector;
import com.granja.dos.huevitos.repository.GalponRepository;
import com.granja.dos.huevitos.repository.SectorRepository;
import com.granja.dos.huevitos.service.GalponService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // Patrón arquitectónico: Inyección por constructor inmutable
public class GalponServiceImpl implements GalponService {

    // Las dependencias ahora son 'final' garantizando que no muten en tiempo de ejecución
    private final GalponRepository galponRepository;
    private final SectorRepository sectorRepository;

    @Override
    public List<GalponResponseDTO> listarGalpones() {
        // 1. Lectura física
        List<Galpon> galponesDB = galponRepository.findAll();
        
        // 2. Mapeo a memoria plana (Destrucción del bucle bidireccional)
        return galponesDB.stream().map(galpon -> {
            GalponResponseDTO dto = new GalponResponseDTO();
            dto.setIdGalpon(galpon.getIdGalpon());
            dto.setNombre(galpon.getNombre());
            dto.setCapacidad(galpon.getCapacidad());
            dto.setEstado(galpon.getEstado());
            
            // Extracción segura de la llave foránea
            if (galpon.getSector() != null) {
                dto.setSectorId(galpon.getSector().getIdSector());
                dto.setNombreSector(galpon.getSector().getNombre());
            }
            return dto;
        }).toList();
    }

    @Override
    public GalponResponseDTO guardarGalpon(String nombre, Integer capacidad, String estado, Integer idSector) {
        Sector sector = sectorRepository.findById(idSector)
                .orElseThrow(() -> new RuntimeException("Integridad fallida: El sector " + idSector + " no existe."));

        Galpon galpon = new Galpon();
        galpon.setNombre(nombre);
        
        // REGLA DE NEGOCIO ESTRICTA: El servidor manda. Ignoramos la 'capacidad' enviada por el navegador.
        galpon.setCapacidad(6000); 
        
        galpon.setEstado(estado != null ? estado : "Activo");
        galpon.setSector(sector);
        
        // Descomenta esta línea solo si NO aplicaste @CreationTimestamp en la entidad Galpon.java
        // galpon.setFechaRegistro(java.time.LocalDate.now()); 

        // 3. Persistencia física
        Galpon galponGuardado = galponRepository.save(galpon);

        // 4. Mapeo de salida
        GalponResponseDTO dto = new GalponResponseDTO();
        dto.setIdGalpon(galponGuardado.getIdGalpon());
        dto.setNombre(galponGuardado.getNombre());
        dto.setCapacidad(galponGuardado.getCapacidad()); // Devolverá 6000 garantizado
        dto.setEstado(galponGuardado.getEstado());
        dto.setSectorId(sector.getIdSector());
        dto.setNombreSector(sector.getNombre());

        return dto;
    }

    // Actualiza también este método si está en tu interfaz
    public List<GalponResponseDTO> listarPorSector(Integer idSector) {
        List<Galpon> galponesDB = galponRepository.findBySectorIdSector(idSector);
        
        return galponesDB.stream().map(galpon -> {
            GalponResponseDTO dto = new GalponResponseDTO();
            dto.setIdGalpon(galpon.getIdGalpon());
            dto.setNombre(galpon.getNombre());
            dto.setCapacidad(galpon.getCapacidad());
            dto.setEstado(galpon.getEstado());
            dto.setSectorId(galpon.getSector().getIdSector());
            dto.setNombreSector(galpon.getSector().getNombre());
            return dto;
        }).toList();
    }
}