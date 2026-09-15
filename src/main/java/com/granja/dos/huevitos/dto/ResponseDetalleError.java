/**
 * 
 */
package com.granja.dos.huevitos.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author WChui
 */
@Getter @Setter
@NoArgsConstructor
public class ResponseDetalleError {

	private int posicion;
    private String campo;
    private String mensaje;

    public ResponseDetalleError(int posicion, String campo, String mensaje) {
        this.posicion = posicion;
        this.campo = campo;
        this.mensaje = mensaje;
    }
    
}
