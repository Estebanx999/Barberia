package com.barberia.dominio;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Cita agendada (HU-01, 05, 06, 07, 12). Agrupa su estado y su regla de cruce
 * de horario (RN-04), ambas sobre la misma entidad.
 * La hora de fin la define la duración del servicio reservado.
 * Guarda clienteId en lugar de referenciar a Cliente para evitar la
 * dependencia circular Cita ↔ Cliente.
 */
@Entity
@Table(name = "cita")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Servicio servicio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCita estado;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "barbero_id")
    private Barbero barbero;

    protected Cita() {
        // requerido por JPA
    }

    public Cita(LocalDate fecha, LocalTime horaInicio, Servicio servicio, Long clienteId, Barbero barbero) {
        if (fecha == null || horaInicio == null) {
            throw new IllegalArgumentException("Fecha y hora son obligatorias");
        }
        if (servicio == null) {
            throw new IllegalArgumentException("El servicio es obligatorio");
        }
        if (clienteId == null || barbero == null) {
            throw new IllegalArgumentException("La cita requiere cliente y barbero");
        }
        LocalTime horaFin = horaInicio.plus(servicio.getDuracion());
        if (!horaFin.isAfter(horaInicio)) {
            throw new IllegalArgumentException("La cita debe terminar el mismo día en que empieza");
        }
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.servicio = servicio;
        this.clienteId = clienteId;
        this.barbero = barbero;
        this.estado = EstadoCita.AGENDADA;
    }

    /** HU-12: marca la cita como atendida. */
    public void marcarAtendida() {
        if (estado == EstadoCita.ATENDIDA) {
            throw new IllegalStateException("La cita ya fue marcada como atendida");
        }
        this.estado = EstadoCita.ATENDIDA;
    }

    /**
     * RN-04: dos citas se cruzan si son del mismo barbero, el mismo día y sus
     * intervalos se solapan. Una cita que termina justo cuando empieza otra no se cruza.
     */
    public boolean seCruzaCon(Cita otra) {
        if (otra == null || !mismoBarbero(otra) || !fecha.equals(otra.fecha)) {
            return false;
        }
        return ocupaFranja(otra.horaInicio, otra.horaFin);
    }

    /** Indica si la cita ocupa alguna parte de la franja [inicio, fin) del mismo día. */
    public boolean ocupaFranja(LocalTime inicio, LocalTime fin) {
        return horaInicio.isBefore(fin) && inicio.isBefore(horaFin);
    }

    private boolean mismoBarbero(Cita otra) {
        if (barbero == otra.barbero) {
            return true;
        }
        Long idPropio = barbero.getId();
        return idPropio != null && idPropio.equals(otra.barbero.getId());
    }

    public Long getId() {
        return id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public Servicio getServicio() {
        return servicio;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public Barbero getBarbero() {
        return barbero;
    }
}
