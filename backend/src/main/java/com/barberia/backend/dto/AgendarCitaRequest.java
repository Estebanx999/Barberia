package com.barberia.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record AgendarCitaRequest(
        Long clienteId,
        Long barberoId,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin
) {}
