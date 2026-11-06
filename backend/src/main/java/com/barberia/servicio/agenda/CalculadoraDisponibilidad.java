package com.barberia.servicio.agenda;

import com.barberia.dominio.Cita;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Calcula las franjas libres de un barbero en un día (HU-02). La usan la
 * consulta de disponibilidad y el agendamiento, que muestra las franjas
 * restantes cuando el horario pedido está ocupado (HU-01).
 */
@Component
public class CalculadoraDisponibilidad {

    private final HorarioAtencion horario;
    private final Clock reloj;

    public CalculadoraDisponibilidad(HorarioAtencion horario, Clock reloj) {
        this.horario = horario;
        this.reloj = reloj;
    }

    /**
     * @param citasDelDia citas del barbero en esa fecha
     * @return franjas futuras que ninguna cita ocupa, en orden de hora
     */
    public List<Franja> franjasLibres(LocalDate fecha, List<Cita> citasDelDia) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        return horario.franjas().stream()
                .filter(franja -> !LocalDateTime.of(fecha, franja.horaInicio()).isBefore(ahora))
                .filter(franja -> citasDelDia.stream()
                        .noneMatch(cita -> cita.ocupaFranja(franja.horaInicio(), franja.horaFin())))
                .toList();
    }
}
