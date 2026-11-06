package com.barberia.web;

import com.barberia.reporte.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Reportes financieros por periodo. Ejemplo: /api/reportes/ingresos?desde=2026-11-01&hasta=2026-11-30 */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteIngresos ingresos;
    private final ReporteGastos gastos;
    private final ReporteUtilidadNeta utilidadNeta;

    public ReporteController(ReporteIngresos ingresos, ReporteGastos gastos, ReporteUtilidadNeta utilidadNeta) {
        this.ingresos = ingresos;
        this.gastos = gastos;
        this.utilidadNeta = utilidadNeta;
    }

    /** HU-15 */
    @GetMapping("/ingresos")
    public ReporteIngresos.Resultado ingresos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ingresos.generar(new Periodo(desde, hasta));
    }

    /** HU-29 */
    @GetMapping("/gastos")
    public ReporteGastos.Resultado gastos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return gastos.generar(new Periodo(desde, hasta));
    }

    /** HU-16 */
    @GetMapping("/utilidad-neta")
    public ReporteUtilidadNeta.Resultado utilidadNeta(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return utilidadNeta.generar(new Periodo(desde, hasta));
    }
}
