package com.barberia.servicio.atencion;

import com.barberia.dominio.Comision;

import java.math.BigDecimal;

public record ComisionVista(
        BigDecimal montoBarbero,
        BigDecimal montoDueno,
        BigDecimal porcentajeBarbero,
        BigDecimal porcentajeDueno) {

    public static ComisionVista de(Comision comision) {
        return new ComisionVista(
                comision.getMontoBarbero(),
                comision.getMontoDueno(),
                comision.getPorcentajeBarbero(),
                comision.getPorcentajeDueno());
    }
}
