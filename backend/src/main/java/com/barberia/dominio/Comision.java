package com.barberia.dominio;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Reparto del valor cobrado entre barbero y dueño (HU-13, RN-01, RN-02).
 * Solo guarda datos; la regla de cálculo vive en las políticas (Strategy).
 */
@Entity
@Table(name = "comision")
public class Comision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montoBarbero;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montoDueno;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeBarbero;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeDueno;

    protected Comision() {
        // requerido por JPA
    }

    public Comision(BigDecimal montoBarbero, BigDecimal montoDueno,
                    BigDecimal porcentajeBarbero, BigDecimal porcentajeDueno) {
        if (montoBarbero == null || montoDueno == null
                || porcentajeBarbero == null || porcentajeDueno == null) {
            throw new IllegalArgumentException("Todos los montos y porcentajes son obligatorios");
        }
        if (montoBarbero.signum() < 0 || montoDueno.signum() < 0) {
            throw new IllegalArgumentException("Los montos no pueden ser negativos");
        }
        this.montoBarbero = montoBarbero;
        this.montoDueno = montoDueno;
        this.porcentajeBarbero = porcentajeBarbero;
        this.porcentajeDueno = porcentajeDueno;
    }

    /** Total repartido; por contrato debe ser igual al valor cobrado (LSP de PoliticaComision). */
    public BigDecimal total() {
        return montoBarbero.add(montoDueno);
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getMontoBarbero() {
        return montoBarbero;
    }

    public BigDecimal getMontoDueno() {
        return montoDueno;
    }

    public BigDecimal getPorcentajeBarbero() {
        return porcentajeBarbero;
    }

    public BigDecimal getPorcentajeDueno() {
        return porcentajeDueno;
    }
}
