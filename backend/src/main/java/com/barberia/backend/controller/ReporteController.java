package com.barberia.backend.controller;

import com.barberia.backend.domain.ReporteFinanciero;
import com.barberia.backend.service.ReporteFinancieroService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** HU-15, HU-16, HU-20. */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteFinancieroService reporteFinancieroService;

    public ReporteController(ReporteFinancieroService reporteFinancieroService) {
        this.reporteFinancieroService = reporteFinancieroService;
    }

    @GetMapping
    public ReporteFinanciero generar(@RequestParam("desde") String desde,
                                      @RequestParam("hasta") String hasta) {
        return reporteFinancieroService.generarReporte(LocalDate.parse(desde), LocalDate.parse(hasta));
    }
}
