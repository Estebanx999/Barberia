package com.barberia.servicio.gasto;

import com.barberia.dominio.Gasto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GastoVista(Long id, String concepto, LocalDate fecha, BigDecimal valor) {

    public static GastoVista de(Gasto gasto) {
        return new GastoVista(gasto.getId(), gasto.getConcepto(), gasto.getFecha(), gasto.getValor());
    }
}
