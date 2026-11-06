package com.barberia.reporte;

import com.barberia.repositorio.CorteRepository;
import com.barberia.repositorio.GastoRepository;
import com.barberia.repositorio.TotalesCortes;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * HU-16: utilidad neta = ingresos brutos - comisiones pagadas - gastos operativos.
 * Obtiene sus propios datos; no depende de los otros reportes.
 */
@Component
public class ReporteUtilidadNeta extends ReportePeriodo<ReporteUtilidadNeta.Datos, ReporteUtilidadNeta.Resultado> {

    private final CorteRepository cortes;
    private final GastoRepository gastos;

    public ReporteUtilidadNeta(CorteRepository cortes, GastoRepository gastos) {
        this.cortes = cortes;
        this.gastos = gastos;
    }

    @Override
    protected Datos obtenerDatos(Periodo periodo) {
        TotalesCortes totales = cortes.totalizar(periodo.desde(), periodo.hasta());
        BigDecimal totalGastos = gastos.sumarValor(periodo.desde(), periodo.hasta());
        return new Datos(totales, totalGastos == null ? BigDecimal.ZERO : totalGastos);
    }

    @Override
    protected Resultado calcular(Periodo periodo, Datos datos) {
        BigDecimal ingresos = datos.cortes().ingresosBrutos();
        BigDecimal comisiones = datos.cortes().comisionesPagadas();
        BigDecimal utilidad = ingresos.subtract(comisiones).subtract(datos.gastos());
        return new Resultado(periodo, ingresos, comisiones, datos.gastos(), utilidad);
    }

    protected record Datos(TotalesCortes cortes, BigDecimal gastos) {
    }

    public record Resultado(
            Periodo periodo,
            BigDecimal ingresosBrutos,
            BigDecimal comisionesPagadas,
            BigDecimal gastosOperativos,
            BigDecimal utilidadNeta) {
    }
}
