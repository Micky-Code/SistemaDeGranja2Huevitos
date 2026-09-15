/**
 * 
 */
package com.granja.dos.huevitos.exception;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author WChui
 */
public class ErrorResponse {
	
	private int status;
	private String mensaje;
	private LocalDateTime fecha;
	private List<String> errores;

	public ErrorResponse(int status, String mensaje) {
		this.status = status;
		this.mensaje = mensaje;
		this.fecha = LocalDateTime.now();
	}

	public ErrorResponse(int status, String mensaje, List<String> errores) {
		this.status = status;
		this.mensaje = mensaje;
		this.errores = errores;
		this.fecha = LocalDateTime.now();
	}

	public int getStatus() {
		return status;
	}

	public String getMensaje() {
		return mensaje;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	public List<String> getErrores() {
		return errores;
	}
}
