package com.barberia.servicio.agenda;

import java.time.LocalDate;
import java.time.LocalTime;

/** Datos para agendar una cita (HU-01). La hora de fin la define la duración del turno. */
public record SolicitudCita(Long clienteId, Long barberoId, LocalDate fecha, LocalTime horaInicio) {
}
