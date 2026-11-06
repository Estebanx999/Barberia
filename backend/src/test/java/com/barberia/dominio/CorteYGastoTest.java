package com.barberia.dominio;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class CorteYGastoTest {

    private static final LocalDate DIA = LocalDate.of(2026, 11, 10);
    private final Barbero barbero = new Barbero("Carlos", null, null);

    @Test
    void corteConCitaMarcaCitaPrevia() {
        Cita cita = new Cita(DIA, LocalTime.of(10, 0), LocalTime.of(10, 30), 1L, barbero);
        Corte corte = new Corte(DIA, "Corte clásico", new BigDecimal("25000"), cita, barbero);
        assertTrue(corte.isTieneCitaPrevia());
        assertSame(cita, corte.getCita());
    }

    @Test
    void corteSinCitaNoMarcaCitaPrevia() {
        Corte corte = new Corte(DIA, "Corte clásico", new BigDecimal("25000"), null, barbero);
        assertFalse(corte.isTieneCitaPrevia());
        assertNull(corte.getCita());
    }

    @Test
    void corteRechazaDatosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new Corte(null, "Corte", BigDecimal.TEN, null, barbero));
        assertThrows(IllegalArgumentException.class, () -> new Corte(DIA, " ", BigDecimal.TEN, null, barbero));
        assertThrows(IllegalArgumentException.class, () -> new Corte(DIA, "Corte", BigDecimal.ZERO, null, barbero));
        assertThrows(IllegalArgumentException.class, () -> new Corte(DIA, "Corte", null, null, barbero));
        assertThrows(IllegalArgumentException.class, () -> new Corte(DIA, "Corte", BigDecimal.TEN, null, null));
    }

    @Test
    void corteAsignaComision() {
        Corte corte = new Corte(DIA, "Corte", new BigDecimal("100"), null, barbero);
        Comision comision = new Comision(new BigDecimal("60"), new BigDecimal("40"),
                new BigDecimal("60"), new BigDecimal("40"));
        corte.asignarComision(comision);
        assertSame(comision, corte.getComision());
        assertThrows(IllegalArgumentException.class, () -> corte.asignarComision(null));
    }

    @Test
    void comisionSumaSusMontos() {
        Comision comision = new Comision(new BigDecimal("15000"), new BigDecimal("10000"),
                new BigDecimal("60"), new BigDecimal("40"));
        assertEquals(new BigDecimal("25000"), comision.total());
    }

    @Test
    void comisionRechazaMontosNegativosOAusentes() {
        assertThrows(IllegalArgumentException.class, () -> new Comision(new BigDecimal("-1"), BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class, () -> new Comision(null, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE));
    }

    @Test
    void gastoRechazaDatosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new Gasto("", DIA, BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, () -> new Gasto("Insumos", null, BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, () -> new Gasto("Insumos", DIA, BigDecimal.ZERO));
    }

    @Test
    void duenoEsBarberoYSeIdentificaComoDueno() {
        Barbero dueno = new Dueno("Oscar", null, null);
        assertTrue(dueno.esDueno());
        assertFalse(barbero.esDueno());
    }

    @Test
    void usuarioRequiereNombre() {
        assertThrows(IllegalArgumentException.class, () -> new Cliente(" ", null, null));
    }
}
