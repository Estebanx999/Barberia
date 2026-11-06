package com.barberia.politica;

import com.barberia.dominio.Comision;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** RN-02: cuando corta el dueño, el 100 % del valor cobrado es para él. */
@Component(NombresPolitica.DUENO)
public class ComisionDueno implements PoliticaComision {

    private static final BigDecimal PORCENTAJE_BARBERO = BigDecimal.ZERO;
    private static final BigDecimal PORCENTAJE_DUENO = new BigDecimal("100");

    @Override
    public Comision calcular(BigDecimal valorCobrado) {
        ValidacionValor.validar(valorCobrado);
        return new Comision(BigDecimal.ZERO, valorCobrado, PORCENTAJE_BARBERO, PORCENTAJE_DUENO);
    }
}
