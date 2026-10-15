package com.barberia.backend.domain.comision;

import com.barberia.backend.domain.Comision;

/**
 * Estrategia concreta: cuando el corte lo realiza el propio dueno, este
 * recibe el 100% del valor cobrado; no aplica el reparto 60/40 de los
 * barberos-empleados (regla confirmada explicitamente en E1).
 */
public class ComisionDuenoStrategy implements CalculadoraComisionStrategy {

    @Override
    public Comision calcular(double valorCobrado) {
        return new Comision(valorCobrado, 0.0, 1.0, 0.0);
    }
}
