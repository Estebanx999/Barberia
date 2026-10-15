package com.barberia.backend.service;

import com.barberia.backend.domain.Barbero;
import com.barberia.backend.domain.Cita;
import com.barberia.backend.domain.Cliente;
import com.barberia.backend.domain.observer.AgendaBarberoObservador;
import com.barberia.backend.domain.observer.NotificacionObservador;
import com.barberia.backend.repository.BarberoRepository;
import com.barberia.backend.repository.CitaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Implementa HU-01 (Agendar cita), HU-02 (Consultar disponibilidad),
 * HU-05/HU-06 (Ver calendario), HU-07 (Validar cruce de horario) y
 * HU-08 (Confirmacion de cita) — solo flujo principal, sin caminos
 * alternos, segun el alcance del prototipo funcional de Entregable 2.
 */
@Service
public class CitaService {

    private final CitaRepository citaRepository;
    private final BarberoRepository barberoRepository;
    private final NotificacionObservador notificacionObservador;
    private final AgendaBarberoObservador agendaBarberoObservador;

    public CitaService(CitaRepository citaRepository,
                        BarberoRepository barberoRepository,
                        NotificacionObservador notificacionObservador,
                        AgendaBarberoObservador agendaBarberoObservador) {
        this.citaRepository = citaRepository;
        this.barberoRepository = barberoRepository;
        this.notificacionObservador = notificacionObservador;
        this.agendaBarberoObservador = agendaBarberoObservador;
    }

    /** HU-02: disponibilidad de un barbero en una fecha dada. */
    public List<Cita> consultarCitasDelDia(Long barberoId, LocalDate fecha) {
        Barbero barbero = barberoRepository.findById(barberoId)
                .orElseThrow(() -> new IllegalArgumentException("Barbero no encontrado"));
        return citaRepository.findByBarberoAndFecha(barbero, fecha);
    }

    /**
     * HU-01 + HU-07: agenda una cita validando que no haya cruce de horario (RN-04).
     * @Transactional: Barbero.citas es LAZY y open-in-view esta desactivado, asi que
     * tieneDisponibilidad() necesita una sesion abierta.
     */
    @Transactional
    public Cita agendarCita(Cliente cliente, Long barberoId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        Barbero barbero = barberoRepository.findById(barberoId)
                .orElseThrow(() -> new IllegalArgumentException("Barbero no encontrado"));

        if (!barbero.tieneDisponibilidad(fecha, horaInicio, horaFin)) {
            throw new IllegalStateException("El barbero ya tiene una cita en ese horario (RN-04)");
        }

        Cita cita = new Cita();
        cita.setCliente(cliente);
        cita.setBarbero(barbero);
        cita.setFecha(fecha);
        cita.setHoraInicio(horaInicio);
        cita.setHoraFin(horaFin);

        // Suscripcion de observadores (patron OBSERVER) antes de confirmar.
        cita.agregarObservador(notificacionObservador);
        cita.agregarObservador(agendaBarberoObservador);

        Cita guardada = citaRepository.save(cita);
        guardada.confirmar(); // HU-08: notifica a los observadores
        return citaRepository.save(guardada);
    }
}
