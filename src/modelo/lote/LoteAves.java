package modelo.lote;
import java.time.*;
public class LoteAves {
	

	    
	    private final Long id;
	    private final String codigoLote;
	    private final String raza;
	    private final LocalDate fechaIngreso;
	    private final int cantidadAvesInicial;
	    private int cantidadAvesActual; // Este valor cambia mediante comportamiento de negocio

	    public LoteAves(Long id, String codigoLote, String raza, LocalDate fechaIngreso, int cantidadAvesInicial) {
	        // 1. Validaciones de entrada (Fail-Fast)
	        if (codigoLote == null || codigoLote.trim().isEmpty()) {
	            throw new IllegalArgumentException("El código del lote no puede estar vacío.");
	        }
	        if (raza == null || raza.trim().isEmpty()) {
	            throw new IllegalArgumentException("La raza de las aves es obligatoria.");
	        }
	        if (fechaIngreso == null) {
	            throw new IllegalArgumentException("La fecha de ingreso no puede ser nula.");
	        }
	        if (cantidadAvesInicial <= 0) {
	            throw new IllegalArgumentException("La cantidad inicial de aves debe ser mayor a cero.");
	        }

	        // 2. Asignación de atributos inmutables y de estado
	        this.id = id;
	        this.codigoLote = codigoLote;
	        this.raza = raza;
	        this.fechaIngreso = fechaIngreso;
	        this.cantidadAvesInicial = cantidadAvesInicial;
	        this.cantidadAvesActual = cantidadAvesInicial; // Al nacer el lote, el actual es igual al inicial
	    }

	    // 3. Comportamiento de Negocio: Control de bajas por mortandad
	    public void registrarBaja(int cantidadMuertas) {
	        if (cantidadMuertas <= 0) {
	            throw new IllegalArgumentException("La cantidad de bajas debe ser mayor a cero.");
	        }
	        if ((this.cantidadAvesActual - cantidadMuertas) < 0) {
	            throw new IllegalArgumentException("No puedes registrar más bajas que las aves que quedan vivas actualmente.");
	        }
	        this.cantidadAvesActual -= cantidadMuertas;
	    }

	    // 4. Getters de Lectura (Sin Setters públicos para los datos principales)
	    public Long getId() {
	        return id;
	    }

	    public String getCodigoLote() {
	        return codigoLote;
	    }

	    public String getRaza() {
	        return raza;
	    }

	    public LocalDate getFechaIngreso() {
	        return fechaIngreso;
	    }

	    public int getCantidadAvesInicial() {
	        return cantidadAvesInicial;
	    }

	    public int getCantidadAvesActual() {
	        return cantidadAvesActual;
	    }	

}
