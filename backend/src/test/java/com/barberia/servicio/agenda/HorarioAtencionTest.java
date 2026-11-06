package com.barberia.servicio.agenda;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HorarioAtencionTest {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    private HorarioAtencion horario(String apertura, String cierre, int minutos) {
        return new HorarioAtencion(LocalTime.parse(apertura), LocalTime.parse(cierre),
                Duration.ofMinutes(minutos), BOGOTA);
    }

    @Test
    void generaFranjasConsecutivasDentroDeLaJornada() {
        List<Franja> franjas = horario("08:00", "20:00", 30).franjas();
        assertEquals(24, franjas.size());
        assertEquals(new Franja(LocalTime.of(8, 0), LocalTime.of(8, 30)), franjas.get(0));
        assertEquals(new Franja(LocalTime.of(19, 30), LocalTime.of(20, 0)), franjas.get(23));
    }

    @Test
    void descartaLaFranjaQueNoAlcanzaACompletarse() {
        List<Franja> franjas = horario("08:00", "09:00", 40).franjas();
        assertEquals(List.of(new Franja(LocalTime.of(8, 0), LocalTime.of(8, 40))), franjas);
    }

    @Test
    void noPasaDeMedianoche() {
        assertEquals(3, horario("22:00", "23:59", 30).franjas().size());
    }

    @Test
    void indicaSiUnTurnoCabeEnLaJornada() {
        HorarioAtencion horario = horario("08:00", "20:00", 30);
        assertTrue(horario.cabeEnJornada(LocalTime.of(8, 0)));
        assertTrue(horario.cabeEnJornada(LocalTime.of(19, 30)));
        assertFalse(horario.cabeEnJornada(LocalTime.of(19, 45)));
        assertFalse(horario.cabeEnJornada(LocalTime.of(7, 59)));
    }

    @Test
    void rechazaConfiguracionInvalida() {
        assertThrows(IllegalArgumentException.class, () -> horario("20:00", "08:00", 30));
        assertThrows(IllegalArgumentException.class, () -> horario("08:00", "20:00", 0));
        assertThrows(NullPointerException.class,
                () -> new HorarioAtencion(null, LocalTime.NOON, Duration.ofMinutes(30), BOGOTA));
    }
}
