package com.barberia.backend.domain;

import com.barberia.backend.domain.observer.CitaSubject;
import com.barberia.backend.domain.observer.ObservadorCita;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Transient;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Cita. Implementa el rol de "Subject" del patron GoF OBSERVER:
 * cuando cambia de estado (confirmada, cancelada, atendida) notifica a los
 * observadores interesados (p. ej. la vista de calendario del barbero,
 * HU-05/HU-06, y el modulo de confirmacion, HU-08) sin acoplar la entidad
 * Cita a esos modulos.
 */
@Entity
@Table(name = "citas")
@Getter
@Setter
@NoArgsConstructor
public class Cita implements CitaSubject {

    public enum Estado { PENDIENTE, CONFIRMADA, ATENDIDA, CANCELADA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Cliente cliente;

    @ManyToOne(optional = false)
    private Barbero barbero;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    private Estado estado = Estado.PENDIENTE;

    @Transient
    private final List<ObservadorCita> observadores = new ArrayList<>();

    public boolean seSolapaCon(LocalTime inicioNueva, LocalTime finNueva) {
        return horaInicio.isBefore(finNueva) && inicioNueva.isBefore(horaFin);
    }

    public void confirmar() {
        this.estado = Estado.CONFIRMADA;
        notificarObservadores();
    }

    public void marcarAtendida() {
        this.estado = Estado.ATENDIDA;
        notificarObservadores();
    }

    public void cancelar() {
        this.estado = Estado.CANCELADA;
        notificarObservadores();
    }

    @Override
    public void agregarObservador(ObservadorCita observador) {
        observadores.add(observador);
    }

    @Override
    public void notificarObservadores() {
        for (ObservadorCita o : observadores) {
            o.actualizar(this);
        }
    }
}
