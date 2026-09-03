package modelo.galpon;


import modelo.paquete.Paquete;
import java.util.*;

	



public class Galpon {
    private final Long id;
    private final String codigo;
    private final SeccionGalpon seccion;
    private final List<Paquete> paquetes; // Lista interna encapsulada

    public Galpon(Long id, String codigo, SeccionGalpon seccion) {
        // 1. Blindaje / Validaciones (Fail-Fast)
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del galpón no puede estar vacío.");
        }
        if (seccion == null) {
            throw new IllegalArgumentException("La sección del galpón es obligatoria.");
        }

        // 2. Asignación inmutable
        this.id = id;
        this.codigo = codigo;
        this.seccion = seccion;
        this.paquetes = new ArrayList<>(); // Nace con su contenedor de producción vacío
    }

    // 3. Comportamiento de Negocio (El objeto controla cómo se modifica su estado)
    public void registrarPaquete(Paquete paquete) {
        if (paquete == null) {
            throw new IllegalArgumentException("El paquete a registrar no puede ser nulo.");
        }
        this.paquetes.add(paquete);
    }

    // 4. Getters de Lectura (Sin Setters)
    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public SeccionGalpon getSeccion() {
        return seccion;
    }

    /**
     * Devuelve una vista de solo lectura de los paquetes. 
     * Esto evita que clases externas modifiquen la lista haciendo .add() o .remove() 
     * por fuera del método de negocio registrarPaquete().
     */
    public List<Paquete> getPaquetes() {
        return Collections.unmodifiableList(paquetes);
    }
}

