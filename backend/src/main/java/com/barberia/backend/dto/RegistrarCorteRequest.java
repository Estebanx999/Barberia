package com.barberia.backend.dto;

public record RegistrarCorteRequest(
        Long barberoId,
        Long clienteId,
        double valorCobrado,
        boolean tieneCitaPrevia
) {}
