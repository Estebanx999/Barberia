package com.barberia.servicio.atencion;

import com.barberia.dominio.Barbero;
import com.barberia.dominio.Comision;
import com.barberia.dominio.Corte;
import com.barberia.reporte.Periodo;
import com.barberia.repositorio.ClienteRepository;
import com.barberia.repositorio.CorteRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.barberia.soporte.DatosDePrueba.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConsultaCortesServiceTest {

    private final CorteRepository cortes = mock(CorteRepository.class);
    private final ClienteRepository clientes = mock(ClienteRepository.class);
    private final ConsultaCortesService servicio = new ConsultaCortesService(cortes, clientes);
    private final Barbero carlos = barbero(2, "Carlos");

    private Corte corte(long id, String valor, String comisionBarbero) {
        Corte corte = conId(Corte.sinCita(DIA, "Corte", new BigDecimal(valor), carlos, 5L), id);
        BigDecimal total = new BigDecimal(valor);
        BigDecimal barbero = new BigDecimal(comisionBarbero);
        corte.asignarComision(new Comision(barbero, total.subtract(barbero), new BigDecimal("60"), new BigDecimal("40")));
        return corte;
    }

    @Test
    void sumaLoCobradoYLaComisionDelBarberoEnElPeriodo() {
        Periodo semana = new Periodo(DIA, DIA.plusDays(6));
        when(cortes.findByBarberoIdAndFechaBetweenOrderByFechaDescIdDesc(2L, semana.desde(), semana.hasta()))
                .thenReturn(List.of(corte(1, "30000", "18000"), corte(2, "20000", "12000")));
        when(clientes.findAllById(any())).thenReturn(List.of(cliente(5, "Juan")));

        ConsultaCortesService.ResumenCortes resumen = servicio.cortesDelBarbero(2L, semana);

        assertEquals(2, resumen.cantidadCortes());
        assertEquals(0, new BigDecimal("50000").compareTo(resumen.totalCobrado()));
        assertEquals(0, new BigDecimal("30000").compareTo(resumen.totalComision()));
        assertEquals("Juan", resumen.cortes().get(0).clienteNombre());
    }

    @Test
    void sinCortesLosTotalesSonCero() {
        Periodo semana = new Periodo(DIA, DIA.plusDays(6));
        when(cortes.findByBarberoIdAndFechaBetweenOrderByFechaDescIdDesc(any(), any(), any())).thenReturn(List.of());
        when(clientes.findAllById(any())).thenReturn(List.of());

        ConsultaCortesService.ResumenCortes resumen = servicio.cortesDelBarbero(2L, semana);

        assertEquals(0, resumen.cantidadCortes());
        assertEquals(0, BigDecimal.ZERO.compareTo(resumen.totalComision()));
    }
}
