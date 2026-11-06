package com.barberia.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** Cuerpo de POST /api/citas/{citaId}/corte (HU-27). Ejemplo: {"servicio":"Corte clásico","valorCobrado":30000} */
public record CobroRequest(
        @NotBlank String servicio,
        @NotNull @Positive BigDecimal valorCobrado) {
}
