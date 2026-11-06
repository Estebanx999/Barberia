package com.barberia.servicio.agenda;

import com.barberia.dominio.Cita;
import com.barberia.dominio.EstadoCita;

import java.time.LocalDate;
import java.time.LocalTime;

/** Cita lista para mostrar en los calendarios, con los nombres de barbero y cliente. */
public record CitaVista(
        Long id,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        EstadoCita estado,
        Long barberoId,
        String barberoNombre,
        Long clienteId,
        String clienteNombre) {

    public static CitaVista de(Cita cita, String clienteNombre) {
        return new CitaVista(
                cita.getId(),
                cita.getFecha(),
                cita.getHoraInicio(),
                cita.getHoraFin(),
                cita.getEstado(),
                cita.getBarbero().getId(),
                cita.getBarbero().getNombre(),
                cita.getClienteId(),
                clienteNombre);
    }
}
