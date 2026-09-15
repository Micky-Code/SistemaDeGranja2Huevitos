/**
 * 
 */
package com.granja.dos.huevitos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.granja.dos.huevitos.dto.ResponseValidacion;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;		
	private final ResponseValidacion response;

	public BadRequestException(String mensaje) {
        super(mensaje);
		this.response = null;
    }

	public BadRequestException(ResponseValidacion response) {
		super(response.getMessage());
		this.response = response;
	}

	public ResponseValidacion getResponse() {
		return response;
	}
	
}
