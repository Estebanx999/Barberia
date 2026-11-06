package com.barberia.servicio.atencion;

import com.barberia.dominio.Corte;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Corte registrado con su comisión calculada. */
public record CorteVista(
        Long id,
        LocalDate fecha,
        String servicio,
        BigDecimal valorCobrado,
        boolean tieneCitaPrevia,
        Long citaId,
        Long barberoId,
        String barberoNombre,
        Long clienteId,
        String clienteNombre,
        ComisionVista comision) {

    public static CorteVista de(Corte corte, String clienteNombre) {
        return new CorteVista(
                corte.getId(),
                corte.getFecha(),
                corte.getServicio(),
                corte.getValorCobrado(),
                corte.isTieneCitaPrevia(),
                corte.getCita() == null ? null : corte.getCita().getId(),
                corte.getBarbero().getId(),
                corte.getBarbero().getNombre(),
                corte.getClienteId(),
                clienteNombre,
                ComisionVista.de(corte.getComision()));
    }
}
