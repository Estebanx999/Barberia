package com.barberia.servicio.agenda;

import java.time.LocalTime;

/** Intervalo de un turno dentro de la jornada. */
public record Franja(LocalTime horaInicio, LocalTime horaFin) {
}
