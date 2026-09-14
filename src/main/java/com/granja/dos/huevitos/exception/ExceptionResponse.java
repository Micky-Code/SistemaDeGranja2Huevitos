package com.granja.dos.huevitos.exception;

import lombok.Getter;

@Getter
public class ExceptionResponse extends RuntimeException {

    private static final long serialVersionUID = -777240506078893789L;

    private final int status;
    private final String mensaje;

    public ExceptionResponse(int status, String mensaje) {
        super(mensaje);
        this.status = status;
        this.mensaje = mensaje;
    }

    public int getStatus() {
        return status;
    }

    public String getMensaje() {
        return mensaje;
    }
}
