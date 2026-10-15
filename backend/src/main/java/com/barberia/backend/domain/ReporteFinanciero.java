package com.barberia.backend.domain;

import lombok.Getter;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

/**
 * ReporteFinanciero: objeto inmutable con varios campos opcionales
 * (ingresos brutos, comisiones pagadas, gastos, utilidad neta, desglose
 * por barbero). Se construye exclusivamente a traves de su Builder
 * interno (patron GoF: BUILDER) para evitar un constructor telescopico y
 * garantizar que el objeto resultante sea siempre consistente
 * (RN-03: utilidad neta = ingresos - comisiones - gastos).
 */
@Getter
public class ReporteFinanciero {

    private final LocalDate desde;
    private final LocalDate hasta;
    private final double ingresosBrutos;
    private final double comisionesPagadas;
    private final double gastosOperativos;
    private final double utilidadNeta;
    private final Map<String, Double> comisionesPorBarbero;

    private ReporteFinanciero(Builder b) {
        this.desde = b.desde;
        this.hasta = b.hasta;
        this.ingresosBrutos = b.ingresosBrutos;
        this.comisionesPagadas = b.comisionesPagadas;
        this.gastosOperativos = b.gastosOperativos;
        this.utilidadNeta = b.ingresosBrutos - b.comisionesPagadas - b.gastosOperativos;
        this.comisionesPorBarbero = Collections.unmodifiableMap(b.comisionesPorBarbero);
    }

    public static Builder builder(LocalDate desde, LocalDate hasta) {
        return new Builder(desde, hasta);
    }

    public static class Builder {
        private final LocalDate desde;
        private final LocalDate hasta;
        private double ingresosBrutos;
        private double comisionesPagadas;
        private double gastosOperativos;
        private Map<String, Double> comisionesPorBarbero = Collections.emptyMap();

        private Builder(LocalDate desde, LocalDate hasta) {
            this.desde = desde;
            this.hasta = hasta;
        }

        public Builder conIngresosBrutos(double v) { this.ingresosBrutos = v; return this; }
        public Builder conComisionesPagadas(double v) { this.comisionesPagadas = v; return this; }
        public Builder conGastosOperativos(double v) { this.gastosOperativos = v; return this; }
        public Builder conDesglosePorBarbero(Map<String, Double> m) { this.comisionesPorBarbero = m; return this; }

        public ReporteFinanciero build() {
            return new ReporteFinanciero(this);
        }
    }
}
