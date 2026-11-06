package com.barberia.dominio;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Servicio realizado y cobrado, con cita previa (HU-27) o sin ella (HU-28).
 * Solo guarda datos: no calcula la comisión (eso es de PoliticaComision)
 * ni coordina el flujo (eso es de AtencionFacade).
 */
@Entity
@Table(name = "corte")
public class Corte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 120)
    private String servicio;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorCobrado;

    @Column(nullable = false)
    private boolean tieneCitaPrevia;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id")
    private Cita cita;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "barbero_id")
    private Barbero barbero;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "comision_id")
    private Comision comision;

    protected Corte() {
        // requerido por JPA
    }

    private Corte(LocalDate fecha, String servicio, BigDecimal valorCobrado,
                  Cita cita, Barbero barbero, Long clienteId) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        if (servicio == null || servicio.isBlank()) {
            throw new IllegalArgumentException("El servicio es obligatorio");
        }
        if (valorCobrado == null || valorCobrado.signum() <= 0) {
            throw new IllegalArgumentException("El valor cobrado debe ser mayor que cero");
        }
        if (barbero == null) {
            throw new IllegalArgumentException("El corte debe asociarse a un barbero");
        }
        if (clienteId == null) {
            throw new IllegalArgumentException("El corte debe asociarse a un cliente");
        }
        this.fecha = fecha;
        this.servicio = servicio.trim();
        this.valorCobrado = valorCobrado;
        this.cita = cita;
        this.tieneCitaPrevia = cita != null;
        this.barbero = barbero;
        this.clienteId = clienteId;
    }

    /**
     * HU-27: corte de una cita ya atendida (HU-12). Toma de la cita la fecha,
     * el barbero y el cliente, para que no puedan contradecirla.
     */
    public static Corte deCita(Cita cita, String servicio, BigDecimal valorCobrado) {
        if (cita == null) {
            throw new IllegalArgumentException("La cita es obligatoria");
        }
        if (cita.getEstado() != EstadoCita.ATENDIDA) {
            throw new IllegalStateException("La cita debe estar marcada como atendida antes de registrar el corte");
        }
        return new Corte(cita.getFecha(), servicio, valorCobrado, cita, cita.getBarbero(), cita.getClienteId());
    }

    /** HU-28: corte de un cliente que llegó sin cita previa. */
    public static Corte sinCita(LocalDate fecha, String servicio, BigDecimal valorCobrado,
                                Barbero barbero, Long clienteId) {
        return new Corte(fecha, servicio, valorCobrado, null, barbero, clienteId);
    }

    public void asignarComision(Comision comision) {
        if (comision == null) {
            throw new IllegalArgumentException("La comisión es obligatoria");
        }
        this.comision = comision;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getServicio() {
        return servicio;
    }

    public BigDecimal getValorCobrado() {
        return valorCobrado;
    }

    public boolean isTieneCitaPrevia() {
        return tieneCitaPrevia;
    }

    public Cita getCita() {
        return cita;
    }

    public Barbero getBarbero() {
        return barbero;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public Comision getComision() {
        return comision;
    }
}
