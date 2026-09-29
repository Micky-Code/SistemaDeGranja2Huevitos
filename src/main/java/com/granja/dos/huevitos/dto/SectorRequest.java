package com.granja.dos.huevitos.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter @Setter @NoArgsConstructor
public class SectorRequest {
	@NotBlank(message = "El nombre es obligatorio")
	private String nombre ;
}
