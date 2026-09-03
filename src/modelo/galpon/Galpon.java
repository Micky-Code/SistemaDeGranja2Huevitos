package modelo.galpon;

import modelo.paquete.Paquete;
import modelo.lote.LoteAves; // 1. Importamos el paquete del lote
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Galpon {
    private final Long id;
    private final String codigo;
    private final SeccionGalpon seccion;
    private final List<Paquete> paquetes; 
    
    // El lote de aves actual que habita este galpón (puede cambiar cuando finalice un ciclo)
    private LoteAves loteActual; 

    public Galpon(Long id, String codigo, SeccionGalpon seccion) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del galpón no puede estar vacío.");
        }
        if (seccion == null) {
            throw new IllegalArgumentException("La sección del galpón es obligatoria.");
        }

        this.id = id;
        this.codigo = codigo;
        this.seccion = seccion;
        this.paquetes = new ArrayList<>();
        this.loteActual = null; // Al nacer el galpón, puede estar vacío (en periodo de descanso)
    }

    // Comportamiento de Negocio: Asignar un lote de aves al galpón
    public void asignarLote(LoteAves nuevoLote) {
        if (nuevoLote == null) {
            throw new IllegalArgumentException("El lote a asignar no puede ser nulo.");
        }
        this.loteActual = nuevoLote;
    }

    // Comportamiento de Negocio: Registrar paquete de producción
    public void registrarPaquete(Paquete paquete) {
        if (paquete == null) {
            throw new IllegalArgumentException("El paquete a registrar no puede ser nulo.");
        }
        // Regla de negocio opcional: ¿Podemos registrar huevos si el galpón no tiene un lote activo?
        if (this.loteActual == null) {
            throw new IllegalStateException("No se pueden registrar paquetes en un galpón que no tiene un lote de aves activo.");
        }
        this.paquetes.add(paquete);
    }

    // Getters
    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public SeccionGalpon getSeccion() { return seccion; }
    public LoteAves getLoteActual() { return loteActual; }
    
    public List<Paquete> getPaquetes() {
        return Collections.unmodifiableList(paquetes);
    }
}
