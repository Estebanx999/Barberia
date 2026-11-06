package com.barberia.dominio;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Gasto operativo del negocio (HU-14): concepto, fecha y valor. */
@Entity
@Table(name = "gasto")
public class Gasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String concepto;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    protected Gasto() {
        // requerido por JPA
    }

    public Gasto(String concepto, LocalDate fecha, BigDecimal valor) {
        if (concepto == null || concepto.isBlank()) {
            throw new IllegalArgumentException("El concepto es obligatorio");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("El valor debe ser mayor que cero");
        }
        this.concepto = concepto.trim();
        this.fecha = fecha;
        this.valor = valor;
    }

    public Long getId() {
        return id;
    }

    public String getConcepto() {
        return concepto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public BigDecimal getValor() {
        return valor;
    }
}
