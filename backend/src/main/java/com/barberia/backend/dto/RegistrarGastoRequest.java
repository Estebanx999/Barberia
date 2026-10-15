package com.barberia.backend.dto;

public record RegistrarGastoRequest(
        Long duenoId,
        double monto,
        String categoria,
        String descripcion
) {}
