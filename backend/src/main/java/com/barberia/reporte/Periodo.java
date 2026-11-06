package com.barberia.reporte;

import java.time.LocalDate;

/** Rango de fechas de un reporte; ambos extremos incluidos. */
public record Periodo(LocalDate desde, LocalDate hasta) {

    public Periodo {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("El periodo requiere fecha inicial y final");
        }
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException("La fecha final del periodo no puede ser anterior a la inicial");
        }
    }
}
