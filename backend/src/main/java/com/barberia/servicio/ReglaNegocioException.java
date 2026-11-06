package com.barberia.servicio;

/** La operación es válida en forma, pero incumple una regla del negocio. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
