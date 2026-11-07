package com.barberia.servicio.cliente;

import com.barberia.repositorio.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static com.barberia.soporte.DatosDePrueba.cliente;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BusquedaClientesServiceTest {

    private final ClienteRepository clientes = mock(ClienteRepository.class);
    private final BusquedaClientesService servicio = new BusquedaClientesService(clientes);

    @Test
    void buscaSinDistinguirMayusculasYLimitaLosResultados() {
        when(clientes.buscarPorNombreOTelefono("%juan%", PageRequest.of(0, 10)))
                .thenReturn(List.of(cliente(5, "Juan Pérez")));

        var resultado = servicio.buscar("  Juan ");

        assertEquals(1, resultado.size());
        assertEquals("Juan Pérez", resultado.get(0).nombre());
    }

    @Test
    void conMenosDeDosCaracteresNoConsulta() {
        assertTrue(servicio.buscar("j").isEmpty());
        assertTrue(servicio.buscar(null).isEmpty());
        verify(clientes, never()).buscarPorNombreOTelefono(any(), any());
    }

    @Test
    void losComodinesEscritosPorElUsuarioSeBuscanLiteralmente() {
        assertEquals("100\\%", BusquedaClientesService.escaparComodines("100%"));
        assertEquals("a\\_b", BusquedaClientesService.escaparComodines("a_b"));
        assertEquals("c\\\\d", BusquedaClientesService.escaparComodines("c\\d"));
    }
}
