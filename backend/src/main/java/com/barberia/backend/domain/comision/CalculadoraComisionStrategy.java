package com.barberia.backend.domain.comision;

import com.barberia.backend.domain.Comision;

/**
 * Patron GoF: STRATEGY.
 *
 * Necesidad concreta del MVP: RN-01/RN-02 dicen que un barbero-empleado
 * recibe 60% y el dueno 40%, pero la regla de negocio confirmada en E1
 * agrega una excepcion real: si quien realiza el corte es el propio dueno,
 * este recibe el 100% (no aplica el reparto 60/40). En el diagrama de
 * clases de E1 esto quedo documentado solo como una nota textual sobre
 * Corte/Comision. En E2 se formaliza como una familia de estrategias
 * intercambiables para que Corte NO necesite conocer ni condicionar con
 * if/else el tipo de barbero que la realizo (evita violar OCP, ver
 * plantilla SOLID).
 */
public interface CalculadoraComisionStrategy {
    Comision calcular(double valorCobrado);
}
