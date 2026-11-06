package com.barberia.politica;

import com.barberia.dominio.Comision;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** RN-01: cuando corta un barbero, 60 % para él y 40 % para el dueño. */
@Component(NombresPolitica.BARBERO)
public class ComisionBarbero implements PoliticaComision {

    private static final BigDecimal PORCENTAJE_BARBERO = new BigDecimal("60");
    private static final BigDecimal PORCENTAJE_DUENO = new BigDecimal("40");
    private static final BigDecimal CIEN = new BigDecimal("100");

    @Override
    public Comision calcular(BigDecimal valorCobrado) {
        ValidacionValor.validar(valorCobrado);
        BigDecimal montoBarbero = valorCobrado.multiply(PORCENTAJE_BARBERO)
                .divide(CIEN, 2, RoundingMode.HALF_UP);
        // El dueño recibe la diferencia, así el reparto suma siempre el valor cobrado.
        BigDecimal montoDueno = valorCobrado.subtract(montoBarbero);
        return new Comision(montoBarbero, montoDueno, PORCENTAJE_BARBERO, PORCENTAJE_DUENO);
    }
}
