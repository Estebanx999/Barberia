package com.barberia.servicio;

/** HU-26: el usuario autenticado no puede acceder a la información o acción pedida. */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
