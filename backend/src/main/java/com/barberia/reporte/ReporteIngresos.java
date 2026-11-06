package com.barberia.reporte;

import com.barberia.repositorio.CorteRepository;
import com.barberia.repositorio.TotalesCortes;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** HU-15: suma de todos los cortes realizados en el periodo. */
@Component
public class ReporteIngresos extends ReportePeriodo<TotalesCortes, ReporteIngresos.Resultado> {

    private final CorteRepository cortes;

    public ReporteIngresos(CorteRepository cortes) {
        this.cortes = cortes;
    }

    @Override
    protected TotalesCortes obtenerDatos(Periodo periodo) {
        return cortes.totalizar(periodo.desde(), periodo.hasta());
    }

    @Override
    protected Resultado calcular(Periodo periodo, TotalesCortes totales) {
        return new Resultado(periodo, totales.cantidadCortes(), totales.ingresosBrutos());
    }

    public record Resultado(Periodo periodo, long cantidadCortes, BigDecimal ingresosBrutos) {
    }
}
