package com.barberia.dominio;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class CorteYGastoTest {

    private static final LocalDate DIA = LocalDate.of(2026, 11, 10);
    private static final long CLIENTE = 7L;
    private final Barbero barbero = new Barbero("Carlos", null, null);

    private Cita cita() {
        return new Cita(DIA, LocalTime.of(10, 0), LocalTime.of(10, 30), CLIENTE, barbero);
    }

    @Test
    void corteDeCitaTomaFechaBarberoYClienteDeLaCita() {
        Cita cita = cita();
        cita.marcarAtendida();

        Corte corte = Corte.deCita(cita, " Corte clásico ", new BigDecimal("25000"));

        assertTrue(corte.isTieneCitaPrevia());
        assertSame(cita, corte.getCita());
        assertSame(barbero, corte.getBarbero());
        assertEquals(CLIENTE, corte.getClienteId());
        assertEquals(DIA, corte.getFecha());
        assertEquals("Corte clásico", corte.getServicio());
    }

    @Test
    void corteDeCitaExigeQueLaCitaEsteAtendida() {
        assertThrows(IllegalStateException.class,
                () -> Corte.deCita(cita(), "Corte", new BigDecimal("25000")));
        assertThrows(IllegalArgumentException.class,
                () -> Corte.deCita(null, "Corte", new BigDecimal("25000")));
    }

    @Test
    void corteSinCitaNoMarcaCitaPrevia() {
        Corte corte = Corte.sinCita(DIA, "Corte clásico", new BigDecimal("25000"), barbero, CLIENTE);
        assertFalse(corte.isTieneCitaPrevia());
        assertNull(corte.getCita());
        assertEquals(CLIENTE, corte.getClienteId());
    }

    @Test
    void corteRechazaDatosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> Corte.sinCita(null, "Corte", BigDecimal.TEN, barbero, CLIENTE));
        assertThrows(IllegalArgumentException.class, () -> Corte.sinCita(DIA, " ", BigDecimal.TEN, barbero, CLIENTE));
        assertThrows(IllegalArgumentException.class, () -> Corte.sinCita(DIA, "Corte", BigDecimal.ZERO, barbero, CLIENTE));
        assertThrows(IllegalArgumentException.class, () -> Corte.sinCita(DIA, "Corte", null, barbero, CLIENTE));
        assertThrows(IllegalArgumentException.class, () -> Corte.sinCita(DIA, "Corte", BigDecimal.TEN, null, CLIENTE));
        assertThrows(IllegalArgumentException.class, () -> Corte.sinCita(DIA, "Corte", BigDecimal.TEN, barbero, null));
    }

    @Test
    void corteAsignaComision() {
        Corte corte = Corte.sinCita(DIA, "Corte", new BigDecimal("100"), barbero, CLIENTE);
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
