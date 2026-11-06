package com.barberia.repositorio;

import java.math.BigDecimal;

/**
 * Totales de los cortes de un periodo, calculados en la base de datos.
 * Las sumas llegan nulas cuando no hay cortes; se normalizan a cero.
 */
public record TotalesCortes(Long cantidadCortes, BigDecimal ingresosBrutos, BigDecimal comisionesPagadas) {

    public TotalesCortes {
        cantidadCortes = cantidadCortes == null ? 0L : cantidadCortes;
        ingresosBrutos = ingresosBrutos == null ? BigDecimal.ZERO : ingresosBrutos;
        comisionesPagadas = comisionesPagadas == null ? BigDecimal.ZERO : comisionesPagadas;
    }
}
