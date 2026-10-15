package com.barberia.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Corte realizado. tieneCitaPrevia=false representa un walk-in: el
 * cliente llega al establecimiento sin cita y, si no hay nadie esperando,
 * se le atiende de una vez (regla confirmada en E1).
 */
@Entity
@Table(name = "cortes")
@Getter
@Setter
@NoArgsConstructor
public class Corte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Cita citaOrigen; // null cuando es walk-in

    @ManyToOne(optional = false)
    private Barbero barbero;

    @ManyToOne(optional = false)
    private Cliente cliente;

    @Column(nullable = false)
    private double valorCobrado;

    @Column(nullable = false)
    private boolean tieneCitaPrevia;

    @Column(nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "comision_id")
    private Comision comision;

    /**
     * Genera la comision delegando en la estrategia del barbero que
     * realizo el corte (patron STRATEGY). Corte no necesita saber si el
     * barbero es un empleado o el dueno.
     */
    public Comision generarComision() {
        this.comision = barbero.calcularComision(valorCobrado);
        return this.comision;
    }
}
