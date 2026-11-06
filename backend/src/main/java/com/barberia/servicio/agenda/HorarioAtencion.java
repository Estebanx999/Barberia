package com.barberia.servicio.agenda;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Jornada de la barbería y duración de cada turno, leídas de application.yml
 * (prefijo barberia.horario). Define las franjas que se ofrecen al agendar.
 */
@ConfigurationProperties(prefix = "barberia.horario")
public record HorarioAtencion(
        @DateTimeFormat(pattern = "HH:mm") LocalTime apertura,
        @DateTimeFormat(pattern = "HH:mm") LocalTime cierre,
        Duration duracionTurno,
        ZoneId zonaHoraria) {

    public HorarioAtencion {
        Objects.requireNonNull(apertura, "barberia.horario.apertura es obligatorio");
        Objects.requireNonNull(cierre, "barberia.horario.cierre es obligatorio");
        Objects.requireNonNull(duracionTurno, "barberia.horario.duracion-turno es obligatorio");
        Objects.requireNonNull(zonaHoraria, "barberia.horario.zona-horaria es obligatorio");
        if (!cierre.isAfter(apertura)) {
            throw new IllegalArgumentException("La hora de cierre debe ser posterior a la de apertura");
        }
        if (duracionTurno.isZero() || duracionTurno.isNegative()) {
            throw new IllegalArgumentException("La duración del turno debe ser positiva");
        }
    }

    /** Franjas consecutivas desde la apertura; solo las que terminan antes o justo al cierre. */
    public List<Franja> franjas() {
        List<Franja> franjas = new ArrayList<>();
        LocalTime inicio = apertura;
        while (cabeEnJornada(inicio)) {
            LocalTime fin = inicio.plus(duracionTurno);
            franjas.add(new Franja(inicio, fin));
            inicio = fin;
        }
        return franjas;
    }

    /** Un turno que empieza en inicio cabe si termina dentro de la jornada (sin pasar de medianoche). */
    public boolean cabeEnJornada(LocalTime inicio) {
        LocalTime fin = inicio.plus(duracionTurno);
        return !inicio.isBefore(apertura) && fin.isAfter(inicio) && !fin.isAfter(cierre);
    }
}
