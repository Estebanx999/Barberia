package com.barberia.servicio.agenda;

import java.util.List;

/** Citas de un barbero en el calendario general del dueño (HU-06). */
public record CalendarioBarbero(Long barberoId, String barberoNombre, List<CitaVista> citas) {
}
