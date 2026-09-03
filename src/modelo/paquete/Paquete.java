package modelo.paquete;
import java.math.*;
import java.time.*;

public class Paquete {
    private final Long id;
    private final int cantidadUnidades;
    private final double pesoTotalGramos;
    private final LocalDateTime fechaPostura;
    private final TipoHuevo tipo;
    
	public Paquete(Long id,int cantidadUnidades,double pesoTotalGramos, LocalDateTime fechaPostura, TipoHuevo tipo) {
		if (cantidadUnidades <= 0) {
		    throw new IllegalArgumentException("La cantidad de unidades debe ser mayor a cero.");
		}
		if (pesoTotalGramos <= 0) {
		    throw new IllegalArgumentException("El peso total no puede ser negativo o cero.");
		}
		if (tipo == null) {
	        throw new IllegalArgumentException("El tipo de huevo no puede ser nulo.");
	    }
		
		
	
		this.id = id;
		this.cantidadUnidades=cantidadUnidades;
		this.pesoTotalGramos=pesoTotalGramos;	
		this.fechaPostura = fechaPostura;
		this.tipo = tipo;
	}

	public Long getId() {
		return id;
	}

	

	public int getCantidadUnidades() {
		return cantidadUnidades;
	}


	public double getPesoTotalGramos() {
		return pesoTotalGramos;
	}



	public LocalDateTime getFechaPostura() {
		return fechaPostura;
	}



	public TipoHuevo getTipo() {
		return tipo;
	}






}


