package com.barberia.politica;

/**
 * Nombres con que se registran las políticas de comisión en Spring. Permiten
 * inyectar cada estrategia por su interfaz sin depender de la clase concreta.
 */
public final class NombresPolitica {

    public static final String BARBERO = "comisionBarbero";
    public static final String DUENO = "comisionDueno";

    private NombresPolitica() {
    }
}
