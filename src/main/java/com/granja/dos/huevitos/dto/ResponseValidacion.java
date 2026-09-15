/**
 * 
 */
package com.granja.dos.huevitos.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author WChui
 */
@Getter @Setter @NoArgsConstructor
public class ResponseValidacion {
	private String message;
	private List<ResponseDetalleError> errors = new ArrayList<>();
}
