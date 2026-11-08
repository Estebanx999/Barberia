package com.barberia.servicio.agenda;

import com.barberia.dominio.Servicio;

/** Servicio reservable tal como lo muestra la pantalla de agendar. */
public record ServicioVista(Servicio codigo, String nombre, long duracionMinutos) {

    public static ServicioVista de(Servicio servicio) {
        return new ServicioVista(servicio, servicio.getNombre(), servicio.getDuracion().toMinutes());
    }
}
