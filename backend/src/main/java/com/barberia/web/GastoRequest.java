package com.barberia.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Cuerpo de POST /api/gastos (HU-14). Ejemplo: {"concepto":"Insumos","fecha":"2026-11-10","valor":50000} */
public record GastoRequest(
        @NotBlank String concepto,
        @NotNull LocalDate fecha,
        @NotNull @Positive BigDecimal valor) {
}
