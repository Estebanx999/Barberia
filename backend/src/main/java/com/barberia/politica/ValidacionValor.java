package com.barberia.politica;

import java.math.BigDecimal;

/** Precondición común a todas las políticas: mismo contrato de entrada (LSP). */
final class ValidacionValor {

    private ValidacionValor() {
    }

    static void validar(BigDecimal valorCobrado) {
        if (valorCobrado == null || valorCobrado.signum() <= 0) {
            throw new IllegalArgumentException("El valor cobrado debe ser mayor que cero");
        }
    }
}
