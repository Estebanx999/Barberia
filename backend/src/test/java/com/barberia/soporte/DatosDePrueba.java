package com.barberia.soporte;

import com.barberia.dominio.Barbero;
import com.barberia.dominio.Cita;
import com.barberia.dominio.Cliente;
import com.barberia.dominio.Dueno;
import com.barberia.servicio.agenda.HorarioAtencion;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.*;

/** Datos y reloj fijo compartidos por las pruebas de servicios. */
public final class DatosDePrueba {

    public static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    public static final LocalDate DIA = LocalDate.of(2026, 11, 10);

    public static final HorarioAtencion HORARIO =
            new HorarioAtencion(LocalTime.of(8, 0), LocalTime.of(20, 0), Duration.ofMinutes(30), BOGOTA);

    private DatosDePrueba() {
    }

    /** Reloj detenido en la fecha y hora indicadas, hora de Bogotá. */
    public static Clock relojEn(LocalDate fecha, LocalTime hora) {
        return Clock.fixed(ZonedDateTime.of(fecha, hora, BOGOTA).toInstant(), BOGOTA);
    }

    public static Barbero barbero(long id, String nombre) {
        return conId(new Barbero(nombre, null, null), id);
    }

    public static Dueno dueno(long id, String nombre) {
        return conId(new Dueno(nombre, null, null), id);
    }

    public static Cliente cliente(long id, String nombre) {
        return conId(new Cliente(nombre, null, null), id);
    }

    public static Cita cita(long id, Barbero barbero, long clienteId, String inicio) {
        return cita(id, barbero, clienteId, DIA, inicio);
    }

    public static Cita cita(long id, Barbero barbero, long clienteId, LocalDate fecha, String inicio) {
        LocalTime horaInicio = LocalTime.parse(inicio);
        Cita cita = new Cita(fecha, horaInicio, horaInicio.plusMinutes(30), clienteId, barbero);
        return conId(cita, id);
    }

    /** El id lo asigna la base de datos; en pruebas unitarias se fija por reflexión. */
    public static <T> T conId(T entidad, long id) {
        ReflectionTestUtils.setField(entidad, "id", id);
        return entidad;
    }
}
