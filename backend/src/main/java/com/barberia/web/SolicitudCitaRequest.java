package com.barberia.web;

import com.barberia.servicio.agenda.SolicitudCita;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/** Cuerpo de POST /api/citas. Ejemplo: {"clienteId":1,"barberoId":2,"fecha":"2026-11-10","horaInicio":"10:00"} */
public record SolicitudCitaRequest(
        @NotNull Long clienteId,
        @NotNull Long barberoId,
        @NotNull LocalDate fecha,
        @NotNull LocalTime horaInicio) {

    public SolicitudCita aSolicitud() {
        return new SolicitudCita(clienteId, barberoId, fecha, horaInicio);
    }
}
