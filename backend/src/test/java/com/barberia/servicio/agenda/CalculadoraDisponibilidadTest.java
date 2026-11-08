package com.barberia.servicio.agenda;

import com.barberia.dominio.Barbero;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

import static com.barberia.soporte.DatosDePrueba.*;
import static org.junit.jupiter.api.Assertions.*;

class CalculadoraDisponibilidadTest {

    private static final Duration MEDIA_HORA = Duration.ofMinutes(30);
    private static final Duration UNA_HORA = Duration.ofMinutes(60);

    private final Barbero carlos = barbero(1, "Carlos");

    @Test
    void excluyeLasFranjasOcupadasYLasQueYaPasaron() {
        var calculadora = new CalculadoraDisponibilidad(HORARIO, relojEn(DIA, LocalTime.of(9, 10)));

        List<Franja> libres = calculadora.franjasLibres(DIA, List.of(cita(1, carlos, 5, "10:00")), MEDIA_HORA);

        assertEquals(20, libres.size(), "24 franjas - 3 pasadas - 1 ocupada");
        assertEquals(LocalTime.of(9, 30), libres.get(0).horaInicio());
        assertTrue(libres.stream().noneMatch(f -> f.horaInicio().equals(LocalTime.of(10, 0))));
    }

    @Test
    void unDiaFuturoSinCitasTieneTodasLasFranjasLibres() {
        var calculadora = new CalculadoraDisponibilidad(HORARIO, relojEn(DIA, LocalTime.of(9, 10)));
        assertEquals(24, calculadora.franjasLibres(DIA.plusDays(1), List.of(), MEDIA_HORA).size());
    }

    @Test
    void unDiaPasadoNoTieneFranjasLibres() {
        var calculadora = new CalculadoraDisponibilidad(HORARIO, relojEn(DIA, LocalTime.of(9, 10)));
        assertTrue(calculadora.franjasLibres(DIA.minusDays(1), List.of(), MEDIA_HORA).isEmpty());
    }

    @Test
    void unServicioDeUnaHoraNecesitaDosTurnosSeguidosLibres() {
        var calculadora = new CalculadoraDisponibilidad(HORARIO, relojEn(DIA, LocalTime.of(7, 0)));

        List<Franja> libres = calculadora.franjasLibres(DIA, List.of(cita(1, carlos, 5, "10:00")), UNA_HORA);

        assertEquals(21, libres.size(), "23 inicios de 08:00 a 19:00 - 09:30 y 10:00, que chocan con la cita");
        assertTrue(libres.stream().noneMatch(f -> f.horaInicio().equals(LocalTime.of(9, 30))));
        assertTrue(libres.stream().anyMatch(f -> f.horaInicio().equals(LocalTime.of(10, 30))));
        assertEquals(LocalTime.of(19, 0), libres.get(libres.size() - 1).horaInicio());
        assertEquals(LocalTime.of(20, 0), libres.get(libres.size() - 1).horaFin());
    }
}
