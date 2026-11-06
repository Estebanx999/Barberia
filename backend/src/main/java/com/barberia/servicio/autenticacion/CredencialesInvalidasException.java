package com.barberia.servicio.autenticacion;

/** HU-22/23/24: correo o contraseña incorrectos. No indica cuál de los dos falló. */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Correo o contraseña incorrectos");
    }
}
