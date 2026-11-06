package com.barberia.servicio.gasto;

import com.barberia.dominio.Gasto;
import com.barberia.repositorio.GastoRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.barberia.soporte.DatosDePrueba.conId;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GastoServiceTest {

    private static final LocalDate DIA = LocalDate.of(2026, 11, 10);

    private final GastoRepository gastos = mock(GastoRepository.class);
    private final GastoService servicio = new GastoService(gastos);

    @Test
    void registraElGastoConConceptoFechaYValor() {
        when(gastos.save(any(Gasto.class))).thenAnswer(invocacion -> conId(invocacion.getArgument(0), 4));

        GastoVista gasto = servicio.registrar(" Insumos ", DIA, new BigDecimal("50000"));

        assertEquals(4L, gasto.id());
        assertEquals("Insumos", gasto.concepto());
        assertEquals(DIA, gasto.fecha());
        assertEquals(0, new BigDecimal("50000").compareTo(gasto.valor()));
    }

    @Test
    void noGuardaUnGastoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar("Insumos", DIA, BigDecimal.ZERO));
        verify(gastos, never()).save(any());
    }
}
