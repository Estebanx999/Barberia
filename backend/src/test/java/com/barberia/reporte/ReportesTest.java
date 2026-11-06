package com.barberia.reporte;

import com.barberia.repositorio.CorteRepository;
import com.barberia.repositorio.GastoRepository;
import com.barberia.repositorio.TotalPorConcepto;
import com.barberia.repositorio.TotalesCortes;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportesTest {

    private static final LocalDate DESDE = LocalDate.of(2026, 11, 1);
    private static final LocalDate HASTA = LocalDate.of(2026, 11, 30);
    private static final Periodo NOVIEMBRE = new Periodo(DESDE, HASTA);

    private final CorteRepository cortes = mock(CorteRepository.class);
    private final GastoRepository gastos = mock(GastoRepository.class);

    private static BigDecimal dinero(String valor) {
        return new BigDecimal(valor);
    }

    private static void assertMonto(String esperado, BigDecimal real) {
        assertEquals(0, dinero(esperado).compareTo(real), () -> "esperado " + esperado + " pero fue " + real);
    }

    // --- Periodo ---

    @Test
    void periodoRechazaFechasInvertidasOAusentes() {
        assertThrows(IllegalArgumentException.class, () -> new Periodo(HASTA, DESDE));
        assertThrows(IllegalArgumentException.class, () -> new Periodo(null, HASTA));
        assertDoesNotThrow(() -> new Periodo(DESDE, DESDE));
    }

    // --- Template Method ---

    @Test
    void elMetodoPlantillaRechazaUnPeriodoNulo() {
        var reporte = new ReporteIngresos(cortes);
        assertThrows(IllegalArgumentException.class, () -> reporte.generar(null));
        verifyNoInteractions(cortes);
    }

    @Test
    void elMetodoPlantillaEjecutaLosPasosEnOrden() {
        List<String> pasos = new ArrayList<>();
        ReportePeriodo<String, String> reporte = new ReportePeriodo<>() {
            @Override
            protected String obtenerDatos(Periodo periodo) {
                pasos.add("obtenerDatos");
                return "datos";
            }

            @Override
            protected String calcular(Periodo periodo, String datos) {
                pasos.add("calcular:" + datos);
                return "resultado";
            }
        };

        assertEquals("resultado", reporte.generar(NOVIEMBRE));
        assertEquals(List.of("obtenerDatos", "calcular:datos"), pasos);
    }

    // --- HU-15 ---

    @Test
    void ingresosSumaLosCortesDelPeriodo() {
        when(cortes.totalizar(DESDE, HASTA)).thenReturn(new TotalesCortes(3L, dinero("75000"), dinero("27000")));

        ReporteIngresos.Resultado resultado = new ReporteIngresos(cortes).generar(NOVIEMBRE);

        assertEquals(3, resultado.cantidadCortes());
        assertMonto("75000", resultado.ingresosBrutos());
        assertEquals(NOVIEMBRE, resultado.periodo());
    }

    @Test
    void ingresosDeUnPeriodoSinCortesEsCero() {
        when(cortes.totalizar(DESDE, HASTA)).thenReturn(new TotalesCortes(0L, null, null));

        ReporteIngresos.Resultado resultado = new ReporteIngresos(cortes).generar(NOVIEMBRE);

        assertEquals(0, resultado.cantidadCortes());
        assertMonto("0", resultado.ingresosBrutos());
    }

    // --- HU-29 ---

    @Test
    void gastosMuestraElTotalYElDetallePorConcepto() {
        when(gastos.totalizarPorConcepto(DESDE, HASTA)).thenReturn(List.of(
                new TotalPorConcepto("Arriendo", dinero("900000"), 1L),
                new TotalPorConcepto("Insumos", dinero("80000"), 2L)));

        ReporteGastos.Resultado resultado = new ReporteGastos(gastos).generar(NOVIEMBRE);

        assertMonto("980000", resultado.totalGastos());
        assertEquals(2, resultado.detalle().size());
        assertEquals("Insumos", resultado.detalle().get(1).concepto());
        assertEquals(2, resultado.detalle().get(1).cantidad());
    }

    @Test
    void gastosDeUnPeriodoSinGastosEsCero() {
        when(gastos.totalizarPorConcepto(DESDE, HASTA)).thenReturn(List.of());

        ReporteGastos.Resultado resultado = new ReporteGastos(gastos).generar(NOVIEMBRE);

        assertMonto("0", resultado.totalGastos());
        assertTrue(resultado.detalle().isEmpty());
    }

    // --- HU-16 ---

    @Test
    void utilidadNetaRestaComisionesYGastosALosIngresos() {
        // 100.000 de ingresos, 48.000 de comisiones a barberos y 30.000 de gastos
        when(cortes.totalizar(DESDE, HASTA)).thenReturn(new TotalesCortes(4L, dinero("100000"), dinero("48000")));
        when(gastos.sumarValor(DESDE, HASTA)).thenReturn(dinero("30000"));

        ReporteUtilidadNeta.Resultado resultado = new ReporteUtilidadNeta(cortes, gastos).generar(NOVIEMBRE);

        assertMonto("100000", resultado.ingresosBrutos());
        assertMonto("48000", resultado.comisionesPagadas());
        assertMonto("30000", resultado.gastosOperativos());
        assertMonto("22000", resultado.utilidadNeta());
    }

    @Test
    void utilidadNetaPuedeSerNegativa() {
        when(cortes.totalizar(DESDE, HASTA)).thenReturn(new TotalesCortes(0L, null, null));
        when(gastos.sumarValor(DESDE, HASTA)).thenReturn(dinero("900000"));

        assertMonto("-900000", new ReporteUtilidadNeta(cortes, gastos).generar(NOVIEMBRE).utilidadNeta());
    }

    @Test
    void utilidadNetaSinMovimientosEsCero() {
        when(cortes.totalizar(DESDE, HASTA)).thenReturn(new TotalesCortes(0L, null, null));
        when(gastos.sumarValor(DESDE, HASTA)).thenReturn(null);

        assertMonto("0", new ReporteUtilidadNeta(cortes, gastos).generar(NOVIEMBRE).utilidadNeta());
    }
}
