package com.barberia.servicio.agenda;

import com.barberia.dominio.Barbero;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static com.barberia.soporte.DatosDePrueba.*;
import static org.junit.jupiter.api.Assertions.*;

class CalculadoraDisponibilidadTest {

    private final Barbero carlos = barbero(1, "Carlos");

    @Test
    void excluyeLasFranjasOcupadasYLasQueYaPasaron() {
        var calculadora = new CalculadoraDisponibilidad(HORARIO, relojEn(DIA, LocalTime.of(9, 10)));

        List<Franja> libres = calculadora.franjasLibres(DIA, List.of(cita(1, carlos, 5, "10:00")));

        assertEquals(20, libres.size(), "24 franjas - 3 pasadas - 1 ocupada");
        assertEquals(LocalTime.of(9, 30), libres.get(0).horaInicio());
        assertTrue(libres.stream().noneMatch(f -> f.horaInicio().equals(LocalTime.of(10, 0))));
    }

    @Test
    void unDiaFuturoSinCitasTieneTodasLasFranjasLibres() {
        var calculadora = new CalculadoraDisponibilidad(HORARIO, relojEn(DIA, LocalTime.of(9, 10)));
        assertEquals(24, calculadora.franjasLibres(DIA.plusDays(1), List.of()).size());
    }

    @Test
    void unDiaPasadoNoTieneFranjasLibres() {
        var calculadora = new CalculadoraDisponibilidad(HORARIO, relojEn(DIA, LocalTime.of(9, 10)));
        assertTrue(calculadora.franjasLibres(DIA.minusDays(1), List.of()).isEmpty());
    }
}
