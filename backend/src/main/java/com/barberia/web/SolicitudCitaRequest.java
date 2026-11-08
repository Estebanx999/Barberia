package com.barberia.web;

import com.barberia.dominio.Servicio;
import com.barberia.servicio.agenda.SolicitudCita;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Cuerpo de POST /api/citas. Ejemplo:
 * {"barberoId":2,"servicio":"CORTE_Y_BARBA","fecha":"2026-11-10","horaInicio":"10:00"}.
 * El cliente es el usuario autenticado: no se recibe en el cuerpo.
 */
public record SolicitudCitaRequest(
        @NotNull Long barberoId,
        @NotNull Servicio servicio,
        @NotNull LocalDate fecha,
        @NotNull LocalTime horaInicio) {

    public SolicitudCita aSolicitud(Long clienteId) {
        return new SolicitudCita(clienteId, barberoId, servicio, fecha, horaInicio);
    }
}
