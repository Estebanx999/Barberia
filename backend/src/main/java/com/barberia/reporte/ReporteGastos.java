package com.barberia.reporte;

import com.barberia.repositorio.GastoRepository;
import com.barberia.repositorio.TotalPorConcepto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/** HU-29: total de gastos operativos del periodo y su detalle por concepto. */
@Component
public class ReporteGastos extends ReportePeriodo<List<TotalPorConcepto>, ReporteGastos.Resultado> {

    private final GastoRepository gastos;

    public ReporteGastos(GastoRepository gastos) {
        this.gastos = gastos;
    }

    @Override
    protected List<TotalPorConcepto> obtenerDatos(Periodo periodo) {
        return gastos.totalizarPorConcepto(periodo.desde(), periodo.hasta());
    }

    @Override
    protected Resultado calcular(Periodo periodo, List<TotalPorConcepto> porConcepto) {
        List<GastoPorConcepto> detalle = porConcepto.stream()
                .map(fila -> new GastoPorConcepto(fila.concepto(), fila.cantidad(), fila.total()))
                .toList();
        BigDecimal total = detalle.stream()
                .map(GastoPorConcepto::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Resultado(periodo, total, detalle);
    }

    public record GastoPorConcepto(String concepto, long cantidad, BigDecimal total) {
    }

    public record Resultado(Periodo periodo, BigDecimal totalGastos, List<GastoPorConcepto> detalle) {
    }
}
