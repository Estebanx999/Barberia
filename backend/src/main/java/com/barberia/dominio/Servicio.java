package com.barberia.dominio;

import java.time.Duration;

/**
 * Servicios que el cliente puede reservar. Cada uno define cuánto tiempo
 * ocupa en la agenda del barbero; el precio se registra al cobrar (HU-27).
 */
public enum Servicio {

    CORTE("Corte de cabello", Duration.ofMinutes(30)),
    BARBA("Perfilado de barba", Duration.ofMinutes(30)),
    CORTE_Y_BARBA("Corte y barba", Duration.ofMinutes(60));

    private final String nombre;
    private final Duration duracion;

    Servicio(String nombre, Duration duracion) {
        this.nombre = nombre;
        this.duracion = duracion;
    }

    public String getNombre() {
        return nombre;
    }

    public Duration getDuracion() {
        return duracion;
    }
}
