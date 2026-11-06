package com.barberia.dominio;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Servicio realizado y cobrado (HU-27, HU-28). Con o sin cita previa.
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

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "comision_id")
    private Comision comision;

    protected Corte() {
        // requerido por JPA
    }

    /** cita puede ser null: corte sin cita previa (HU-28). */
    public Corte(LocalDate fecha, String servicio, BigDecimal valorCobrado, Cita cita, Barbero barbero) {
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
        this.fecha = fecha;
        this.servicio = servicio;
        this.valorCobrado = valorCobrado;
        this.cita = cita;
        this.tieneCitaPrevia = cita != null;
        this.barbero = barbero;
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

    public Comision getComision() {
        return comision;
    }
}
