package com.barberia.politica;

import com.barberia.dominio.Comision;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class PoliticaComisionTest {

    private final PoliticaComision barbero = new ComisionBarbero();
    private final PoliticaComision dueno = new ComisionDueno();

    private static void assertMonto(String esperado, BigDecimal real) {
        assertEquals(0, new BigDecimal(esperado).compareTo(real), () -> "esperado " + esperado + " pero fue " + real);
    }

    @Test
    void barberoRecibeSesentaPorCientoYDuenoCuarenta() {
        // Ejemplo de la HU-13: corte de $30.000
        Comision comision = barbero.calcular(new BigDecimal("30000"));
        assertMonto("18000", comision.getMontoBarbero());
        assertMonto("12000", comision.getMontoDueno());
        assertMonto("60", comision.getPorcentajeBarbero());
        assertMonto("40", comision.getPorcentajeDueno());
    }

    @Test
    void duenoRecibeElCienPorCientoCuandoCortaEl() {
        Comision comision = dueno.calcular(new BigDecimal("30000"));
        assertMonto("0", comision.getMontoBarbero());
        assertMonto("30000", comision.getMontoDueno());
        assertMonto("100", comision.getPorcentajeDueno());
    }

    @Test
    void redondeaElMontoDelBarberoADosDecimales() {
        Comision comision = barbero.calcular(new BigDecimal("33333.33"));
        assertMonto("20000.00", comision.getMontoBarbero());
        assertMonto("13333.33", comision.getMontoDueno());
    }

    static Stream<Arguments> politicasYValores() {
        List<PoliticaComision> politicas = List.of(new ComisionBarbero(), new ComisionDueno());
        List<String> valores = List.of("1", "0.01", "25000", "33333.33", "99999.99");
        return politicas.stream().flatMap(p -> valores.stream().map(v -> Arguments.of(p, v)));
    }

    /** Contrato de PoliticaComision (LSP): cualquier política reparte exactamente el valor cobrado. */
    @ParameterizedTest
    @MethodSource("politicasYValores")
    void todaPoliticaReparteExactamenteElValorCobrado(PoliticaComision politica, String valor) {
        Comision comision = politica.calcular(new BigDecimal(valor));
        assertMonto(valor, comision.total());
        assertTrue(comision.getMontoBarbero().signum() >= 0);
        assertTrue(comision.getMontoDueno().signum() >= 0);
        assertMonto("100", comision.getPorcentajeBarbero().add(comision.getPorcentajeDueno()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-100"})
    void todaPoliticaRechazaValoresNoPositivos(String valor) {
        assertThrows(IllegalArgumentException.class, () -> barbero.calcular(new BigDecimal(valor)));
        assertThrows(IllegalArgumentException.class, () -> dueno.calcular(new BigDecimal(valor)));
    }

    @Test
    void todaPoliticaRechazaValorNulo() {
        assertThrows(IllegalArgumentException.class, () -> barbero.calcular(null));
        assertThrows(IllegalArgumentException.class, () -> dueno.calcular(null));
    }
}
