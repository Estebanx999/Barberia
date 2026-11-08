package com.barberia.dominio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class CitaTest {

    private static final LocalDate DIA = LocalDate.of(2026, 11, 10);

    private Barbero barbero;
    private Barbero otroBarbero;

    @BeforeEach
    void preparar() {
        barbero = new Barbero("Carlos", "carlos@barberia.com", "3001112233");
        otroBarbero = new Barbero("Andrés", "andres@barberia.com", "3004445566");
    }

    private Cita cita(LocalDate fecha, String inicio, Servicio servicio, Barbero b) {
        return new Cita(fecha, LocalTime.parse(inicio), servicio, 1L, b);
    }

    /** Cita de media hora (corte de cabello). */
    private Cita cita(LocalDate fecha, String inicio, Barbero b) {
        return cita(fecha, inicio, Servicio.CORTE, b);
    }

    @Test
    void nuevaCitaQuedaAgendada() {
        assertEquals(EstadoCita.AGENDADA, cita(DIA, "10:00", barbero).getEstado());
    }

    @Test
    void marcarAtendidaCambiaElEstado() {
        Cita cita = cita(DIA, "10:00", barbero);
        cita.marcarAtendida();
        assertEquals(EstadoCita.ATENDIDA, cita.getEstado());
    }

    @Test
    void noSePuedeMarcarAtendidaDosVeces() {
        Cita cita = cita(DIA, "10:00", barbero);
        cita.marcarAtendida();
        assertThrows(IllegalStateException.class, cita::marcarAtendida);
    }

    @Test
    void laHoraDeFinLaDefineLaDuracionDelServicio() {
        assertEquals(LocalTime.of(10, 30), cita(DIA, "10:00", Servicio.BARBA, barbero).getHoraFin());
        assertEquals(LocalTime.of(11, 0), cita(DIA, "10:00", Servicio.CORTE_Y_BARBA, barbero).getHoraFin());
        assertEquals(Servicio.CORTE_Y_BARBA, cita(DIA, "10:00", Servicio.CORTE_Y_BARBA, barbero).getServicio());
    }

    @Test
    void rechazaUnaCitaQuePasaDeMedianoche() {
        assertThrows(IllegalArgumentException.class, () -> cita(DIA, "23:30", Servicio.CORTE_Y_BARBA, barbero));
    }

    @Test
    void rechazaDatosObligatoriosAusentes() {
        assertThrows(IllegalArgumentException.class,
                () -> new Cita(null, LocalTime.NOON, Servicio.CORTE, 1L, barbero));
        assertThrows(IllegalArgumentException.class,
                () -> new Cita(DIA, LocalTime.NOON, null, 1L, barbero));
        assertThrows(IllegalArgumentException.class,
                () -> new Cita(DIA, LocalTime.NOON, Servicio.CORTE, null, barbero));
        assertThrows(IllegalArgumentException.class,
                () -> new Cita(DIA, LocalTime.NOON, Servicio.CORTE, 1L, null));
    }

    @Test
    void seCruzanLasCitasSolapadasDelMismoBarberoYDia() {
        Cita base = cita(DIA, "10:00", barbero);
        assertTrue(base.seCruzaCon(cita(DIA, "10:15", barbero)));
        assertTrue(base.seCruzaCon(cita(DIA, "09:45", barbero)));
        assertTrue(base.seCruzaCon(cita(DIA, "10:00", barbero)));
        assertTrue(base.seCruzaCon(cita(DIA, "09:30", Servicio.CORTE_Y_BARBA, barbero)));
    }

    @Test
    void elCruceEsSimetrico() {
        Cita a = cita(DIA, "10:00", barbero);
        Cita b = cita(DIA, "10:15", barbero);
        assertEquals(a.seCruzaCon(b), b.seCruzaCon(a));
    }

    @Test
    void citasContiguasNoSeCruzan() {
        Cita base = cita(DIA, "10:00", barbero);
        assertFalse(base.seCruzaCon(cita(DIA, "10:30", barbero)));
        assertFalse(base.seCruzaCon(cita(DIA, "09:30", barbero)));
    }

    @Test
    void noSeCruzanCitasDeOtroBarberoNiDeOtroDia() {
        Cita base = cita(DIA, "10:00", barbero);
        assertFalse(base.seCruzaCon(cita(DIA, "10:00", otroBarbero)));
        assertFalse(base.seCruzaCon(cita(DIA.plusDays(1), "10:00", barbero)));
    }

    @Test
    void noSeCruzaConNulo() {
        assertFalse(cita(DIA, "10:00", barbero).seCruzaCon(null));
    }

    @Test
    void ocupaLaFranjaQueSeSolapaConSuHorario() {
        Cita base = cita(DIA, "10:00", barbero);
        assertTrue(base.ocupaFranja(LocalTime.of(10, 0), LocalTime.of(10, 30)));
        assertTrue(base.ocupaFranja(LocalTime.of(10, 15), LocalTime.of(10, 45)));
        assertFalse(base.ocupaFranja(LocalTime.of(10, 30), LocalTime.of(11, 0)));
        assertFalse(base.ocupaFranja(LocalTime.of(9, 30), LocalTime.of(10, 0)));
    }
}
