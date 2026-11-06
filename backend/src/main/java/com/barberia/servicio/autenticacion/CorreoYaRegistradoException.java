package com.barberia.servicio.autenticacion;

import com.barberia.servicio.ReglaNegocioException;

/** HU-21: no se crean cuentas duplicadas para un mismo correo. */
public class CorreoYaRegistradoException extends ReglaNegocioException {

    public CorreoYaRegistradoException() {
        super("El correo ya está registrado");
    }
}
