package com.barberia.politica;

import com.barberia.dominio.Comision;

import java.math.BigDecimal;

/**
 * Patrón Strategy: regla para repartir el valor cobrado de un corte (HU-13).
 * Contrato común a todas las implementaciones (LSP): el total de la comisión
 * devuelta es exactamente el valor cobrado y ningún monto es negativo.
 */
public interface PoliticaComision {

    Comision calcular(BigDecimal valorCobrado);
}
