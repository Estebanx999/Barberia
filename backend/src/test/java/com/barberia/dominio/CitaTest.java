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

    private Cita cita(LocalDate fecha, String inicio, String fin, Barbero b) {
        return new Cita(fecha, LocalTime.parse(inicio), LocalTime.parse(fin), 1L, b);
    }

    @Test
    void nuevaCitaQuedaAgendada() {
        assertEquals(EstadoCita.AGENDADA, cita(DIA, "10:00", "10:30", barbero).getEstado());
    }

    @Test
    void marcarAtendidaCambiaElEstado() {
        Cita cita = cita(DIA, "10:00", "10:30", barbero);
        cita.marcarAtendida();
        assertEquals(EstadoCita.ATENDIDA, cita.getEstado());
    }

    @Test
    void noSePuedeMarcarAtendidaDosVeces() {
        Cita cita = cita(DIA, "10:00", "10:30", barbero);
        cita.marcarAtendida();
        assertThrows(IllegalStateException.class, cita::marcarAtendida);
    }

    @Test
    void rechazaHoraFinNoPosteriorALaDeInicio() {
        assertThrows(IllegalArgumentException.class, () -> cita(DIA, "10:30", "10:30", barbero));
        assertThrows(IllegalArgumentException.class, () -> cita(DIA, "11:00", "10:30", barbero));
    }

    @Test
    void rechazaDatosObligatoriosAusentes() {
        assertThrows(IllegalArgumentException.class,
                () -> new Cita(null, LocalTime.NOON, LocalTime.NOON.plusHours(1), 1L, barbero));
        assertThrows(IllegalArgumentException.class,
                () -> new Cita(DIA, LocalTime.NOON, LocalTime.NOON.plusHours(1), null, barbero));
        assertThrows(IllegalArgumentException.class,
                () -> new Cita(DIA, LocalTime.NOON, LocalTime.NOON.plusHours(1), 1L, null));
    }

    @Test
    void seCruzanLasCitasSolapadasDelMismoBarberoYDia() {
        Cita base = cita(DIA, "10:00", "10:30", barbero);
        assertTrue(base.seCruzaCon(cita(DIA, "10:15", "10:45", barbero)));
        assertTrue(base.seCruzaCon(cita(DIA, "09:45", "10:15", barbero)));
        assertTrue(base.seCruzaCon(cita(DIA, "10:00", "10:30", barbero)));
        assertTrue(base.seCruzaCon(cita(DIA, "09:00", "11:00", barbero)));
    }

    @Test
    void elCruceEsSimetrico() {
        Cita a = cita(DIA, "10:00", "10:30", barbero);
        Cita b = cita(DIA, "10:15", "10:45", barbero);
        assertEquals(a.seCruzaCon(b), b.seCruzaCon(a));
    }

    @Test
    void citasContiguasNoSeCruzan() {
        Cita base = cita(DIA, "10:00", "10:30", barbero);
        assertFalse(base.seCruzaCon(cita(DIA, "10:30", "11:00", barbero)));
        assertFalse(base.seCruzaCon(cita(DIA, "09:30", "10:00", barbero)));
    }

    @Test
    void noSeCruzanCitasDeOtroBarberoNiDeOtroDia() {
        Cita base = cita(DIA, "10:00", "10:30", barbero);
        assertFalse(base.seCruzaCon(cita(DIA, "10:00", "10:30", otroBarbero)));
        assertFalse(base.seCruzaCon(cita(DIA.plusDays(1), "10:00", "10:30", barbero)));
    }

    @Test
    void noSeCruzaConNulo() {
        assertFalse(cita(DIA, "10:00", "10:30", barbero).seCruzaCon(null));
    }

    @Test
    void ocupaLaFranjaQueSeSolapaConSuHorario() {
        Cita base = cita(DIA, "10:00", "10:30", barbero);
        assertTrue(base.ocupaFranja(LocalTime.of(10, 0), LocalTime.of(10, 30)));
        assertTrue(base.ocupaFranja(LocalTime.of(10, 15), LocalTime.of(10, 45)));
        assertFalse(base.ocupaFranja(LocalTime.of(10, 30), LocalTime.of(11, 0)));
        assertFalse(base.ocupaFranja(LocalTime.of(9, 30), LocalTime.of(10, 0)));
    }
}
