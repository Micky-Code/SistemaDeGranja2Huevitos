package com.granja.dos.huevitos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.granja.dos.huevitos.config.GlobalExceptionHandler;
import com.granja.dos.huevitos.dto.ResponseValidacion;

class GlobalExceptionHandlerTest {

	@Test
	void debeRetornarResponseValidacionConLaPosicionDelElemento() throws Exception {
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(List.of(), "requests");
		bindingResult
				.addError(new FieldError("requests", "[1].idUsuarioCreacion", "idUsuarioCreacion, no puede ser nulo"));
		bindingResult.addError(
				new FieldError("requests", "[2].consignado", "consignado, no debe tener más de 250 caracteres"));

		Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("endpoint", List.class);
		MethodParameter parameter = new MethodParameter(method, 0);
		MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

		ResponseEntity<ResponseValidacion> entity = new GlobalExceptionHandler().manejarValidacion(exception);

		assertEquals(400, entity.getStatusCode().value());
		assertNotNull(entity.getBody());
		assertEquals("Se encontraron errores de validación.", entity.getBody().getMessage());
		assertEquals(2, entity.getBody().getErrors().size());

		assertEquals(2, entity.getBody().getErrors().get(0).getPosicion());
		assertEquals("idUsuarioCreacion", entity.getBody().getErrors().get(0).getCampo());
		assertEquals("idUsuarioCreacion, no puede ser nulo", entity.getBody().getErrors().get(0).getMensaje());

		assertEquals(3, entity.getBody().getErrors().get(1).getPosicion());
		assertEquals("consignado", entity.getBody().getErrors().get(1).getCampo());
	}

	@SuppressWarnings("unused")
	private void endpoint(List<?> requests) {
		// Método auxiliar requerido para construir MethodParameter en el test.
	}

}
