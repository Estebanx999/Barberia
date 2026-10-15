package com.barberia.backend.service;

import com.barberia.backend.domain.Corte;
import com.barberia.backend.domain.Gasto;
import com.barberia.backend.domain.ReporteFinanciero;
import com.barberia.backend.repository.CorteRepository;
import com.barberia.backend.repository.GastoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementa HU-15 (Ingresos brutos por periodo), HU-16 (Utilidad neta
 * por periodo) y da soporte a HU-20 (Exportar reporte). Usa el patron
 * BUILDER para ensamblar el ReporteFinanciero (RN-03).
 */
@Service
public class ReporteFinancieroService {

    private final CorteRepository corteRepository;
    private final GastoRepository gastoRepository;

    public ReporteFinancieroService(CorteRepository corteRepository, GastoRepository gastoRepository) {
        this.corteRepository = corteRepository;
        this.gastoRepository = gastoRepository;
    }

    public ReporteFinanciero generarReporte(LocalDate desde, LocalDate hasta) {
        List<Corte> cortes = corteRepository.findByFechaHoraBetween(
                desde.atStartOfDay(), LocalDateTime.of(hasta, LocalTime.MAX));
        List<Gasto> gastos = gastoRepository.findByFechaBetween(desde, hasta);

        double ingresosBrutos = cortes.stream().mapToDouble(Corte::getValorCobrado).sum();
        // Comisiones pagadas a los barberos-empleados (60%); cuando el corte lo
        // realiza el dueno, montoBarbero = 0 (ComisionDuenoStrategy), por lo que
        // automaticamente no se resta nada en ese caso (RN-01/RN-02/regla del dueno).
        double comisionesPagadasABarberos = cortes.stream()
                .mapToDouble(c -> c.getComision() != null ? c.getComision().getMontoBarbero() : 0)
                .sum();
        double gastosOperativos = gastos.stream().mapToDouble(Gasto::getMonto).sum();

        Map<String, Double> comisionesPorBarbero = new HashMap<>();
        for (Corte c : cortes) {
            if (c.getComision() == null) continue;
            String nombre = c.getBarbero().getNombre();
            comisionesPorBarbero.merge(nombre, c.getComision().getMontoBarbero(), Double::sum);
        }

        // utilidad neta (RN-03) = ingresos - comisiones pagadas a barberos - gastos
        return ReporteFinanciero.builder(desde, hasta)
                .conIngresosBrutos(ingresosBrutos)
                .conComisionesPagadas(comisionesPagadasABarberos)
                .conGastosOperativos(gastosOperativos)
                .conDesglosePorBarbero(comisionesPorBarbero)
                .build();
    }
}
