package com.barberia.servicio.agenda;

import com.barberia.dominio.Servicio;

import java.time.LocalDate;
import java.time.LocalTime;

/** Datos para agendar una cita (HU-01). La hora de fin la define la duración del servicio. */
public record SolicitudCita(Long clienteId, Long barberoId, Servicio servicio, LocalDate fecha, LocalTime horaInicio) {
}
