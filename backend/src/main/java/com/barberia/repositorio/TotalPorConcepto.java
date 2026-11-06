package com.barberia.repositorio;

import java.math.BigDecimal;

/** Suma y cantidad de los gastos de un concepto en un periodo. */
public record TotalPorConcepto(String concepto, BigDecimal total, Long cantidad) {
}
