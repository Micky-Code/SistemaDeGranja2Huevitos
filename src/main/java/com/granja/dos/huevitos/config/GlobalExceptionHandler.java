/**
 * 
 */
package com.granja.dos.huevitos.config;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import com.granja.dos.huevitos.dto.ResponseValidacion;
import com.granja.dos.huevitos.dto.ResponseDetalleError;
import com.granja.dos.huevitos.exception.BadRequestException;
import com.granja.dos.huevitos.exception.ErrorResponse;
import com.granja.dos.huevitos.exception.ExceptionResponse;


import lombok.extern.slf4j.Slf4j;

/**
 * 
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ErrorResponse> manejarAutenticacion(org.springframework.security.core.AuthenticationException ex) {
        return ResponseEntity.status(401).body(new ErrorResponse(401, "Usuario o contraseña incorrectos."));
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> manejarAccesoDenegado(org.springframework.security.access.AccessDeniedException ex) {
        return ResponseEntity.status(403).body(new ErrorResponse(403, "No tiene permisos para esta operación."));
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> manejarJsonInvalido(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(400, "El cuerpo JSON no es válido."));
    }

	private static final Pattern LIST_FIELD_PATTERN =
			Pattern.compile(".*\\[(\\d+)]\\.?(.+)");

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ResponseValidacion> manejarValidacion(MethodArgumentNotValidException ex) {
		ResponseValidacion response = new ResponseValidacion();
		response.setMessage("Se encontraron errores de validación.");
		response.setErrors(ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(this::crearResponseError)
				.toList());

		return ResponseEntity.badRequest().body(response);
	}

	@ExceptionHandler(HandlerMethodValidationException.class)
	public ResponseEntity<ResponseValidacion> manejarValidacionMetodo(HandlerMethodValidationException ex) {
		ResponseValidacion response = new ResponseValidacion();
		response.setMessage("Se encontraron errores de validación.");
		response.setErrors(ex.getAllErrors()
				.stream()
				.map(this::crearResponseError)
				.toList());

		return ResponseEntity.badRequest().body(response);
	}

	private ResponseDetalleError crearResponseError(FieldError fieldError) {
		String campo = fieldError.getField();
		int posicion = 1;
		Matcher matcher = LIST_FIELD_PATTERN.matcher(campo);

		if (matcher.matches()) {
			posicion = Integer.parseInt(matcher.group(1)) + 1;
			campo = matcher.group(2);
		}

		String mensaje = fieldError.getDefaultMessage() == null
				? "Valor inválido."
				: fieldError.getDefaultMessage();

		return new ResponseDetalleError(posicion, campo, mensaje);
	}

	private ResponseDetalleError crearResponseError(MessageSourceResolvable error) {
		if (error instanceof FieldError fieldError) {
			return crearResponseError(fieldError);
		}

		String mensaje = error.getDefaultMessage() == null
				? "Valor inválido."
				: error.getDefaultMessage();

		return new ResponseDetalleError(1, "request", mensaje);
	}
	
	@ExceptionHandler(ExceptionResponse.class)
	public ResponseEntity<ErrorResponse> manejarExceptionResponse(ExceptionResponse ex) {
		log.error(ex.getMessage(), ex);
		ErrorResponse error = new ErrorResponse(ex.getStatus(), ex.getMensaje());
		return ResponseEntity.status(ex.getStatus()).body(error);
	}
	
	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<Object> handleBadRequest(BadRequestException ex) {
		log.error(ex.getMessage(), ex);
		if (ex.getResponse() != null) {
			return ResponseEntity.badRequest().body(ex.getResponse());
		}

		Map<String, Object> body = new HashMap<>();
		body.put("code", HttpStatus.BAD_REQUEST.value());
		body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
	}	

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> manejarExceptionGeneral(Exception ex) {
		log.error(ex.getMessage(), ex); 
		ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error interno del servidor");
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	}

}
