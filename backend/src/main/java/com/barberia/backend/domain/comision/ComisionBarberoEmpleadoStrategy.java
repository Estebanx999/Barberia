package com.barberia.backend.domain.comision;

import com.barberia.backend.domain.Comision;

/** Estrategia concreta: aplica RN-01/RN-02 (60% barbero / 40% dueno). */
public class ComisionBarberoEmpleadoStrategy implements CalculadoraComisionStrategy {

    public static final double PORCENTAJE_BARBERO = 0.60;
    public static final double PORCENTAJE_DUENO = 0.40;

    @Override
    public Comision calcular(double valorCobrado) {
        double montoBarbero = valorCobrado * PORCENTAJE_BARBERO;
        double montoDueno = valorCobrado * PORCENTAJE_DUENO;
        return new Comision(montoBarbero, montoDueno, PORCENTAJE_BARBERO, PORCENTAJE_DUENO);
    }
}
