package com.barberia.servicio.atencion;

import com.barberia.dominio.*;
import com.barberia.politica.ComisionBarbero;
import com.barberia.politica.ComisionDueno;
import com.barberia.repositorio.BarberoRepository;
import com.barberia.repositorio.CitaRepository;
import com.barberia.repositorio.ClienteRepository;
import com.barberia.repositorio.CorteRepository;
import com.barberia.servicio.RecursoNoEncontradoException;
import com.barberia.servicio.ReglaNegocioException;
import com.barberia.servicio.agenda.CitaVista;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Optional;

import static com.barberia.soporte.DatosDePrueba.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AtencionFacadeTest {

    private static final BigDecimal TREINTA_MIL = new BigDecimal("30000");

    private final CitaRepository citas = mock(CitaRepository.class);
    private final CorteRepository cortes = mock(CorteRepository.class);
    private final BarberoRepository barberos = mock(BarberoRepository.class);
    private final ClienteRepository clientes = mock(ClienteRepository.class);
    private final Barbero carlos = barbero(2, "Carlos");
    private final Dueno oscar = dueno(3, "Oscar");
    private final Cliente juan = cliente(5, "Juan");

    private AtencionFacade fachada;

    @BeforeEach
    void preparar() {
        fachada = new AtencionFacade(citas, cortes, barberos, clientes,
                new ComisionBarbero(), new ComisionDueno(), relojEn(DIA, LocalTime.of(12, 0)));
        when(clientes.findById(5L)).thenReturn(Optional.of(juan));
        when(cortes.save(any(Corte.class))).thenAnswer(invocacion -> conId(invocacion.getArgument(0), 20));
    }

    private Cita citaRegistrada(Barbero barbero, boolean atendida) {
        Cita cita = cita(1, barbero, 5, "10:00");
        if (atendida) {
            cita.marcarAtendida();
        }
        when(citas.findByIdParaAtender(1L)).thenReturn(Optional.of(cita));
        return cita;
    }

    private static void assertMonto(String esperado, BigDecimal real) {
        assertEquals(0, new BigDecimal(esperado).compareTo(real), () -> "esperado " + esperado + " pero fue " + real);
    }

    // --- HU-12 ---

    @Test
    void marcaLaCitaDeHoyComoAtendida() {
        Cita cita = citaRegistrada(carlos, false);

        CitaVista vista = fachada.marcarAtendida(1L);

        assertEquals(EstadoCita.ATENDIDA, cita.getEstado());
        assertEquals(EstadoCita.ATENDIDA, vista.estado());
        assertEquals("Juan", vista.clienteNombre());
    }

    @Test
    void noMarcaUnaCitaFutura() {
        Cita futura = cita(1, carlos, 5, DIA.plusDays(1), "10:00");
        when(citas.findByIdParaAtender(1L)).thenReturn(Optional.of(futura));

        assertThrows(ReglaNegocioException.class, () -> fachada.marcarAtendida(1L));
        assertEquals(EstadoCita.AGENDADA, futura.getEstado());
    }

    @Test
    void noMarcaDosVecesLaMismaCita() {
        citaRegistrada(carlos, true);
        assertThrows(ReglaNegocioException.class, () -> fachada.marcarAtendida(1L));
    }

    @Test
    void citaInexistenteResponde404() {
        when(citas.findByIdParaAtender(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> fachada.marcarAtendida(99L));
        assertThrows(RecursoNoEncontradoException.class, () -> fachada.registrarAtencion(99L, "Corte", TREINTA_MIL));
    }

    // --- HU-27 + HU-13 ---

    @Test
    void registraElCobroYAplicaLaPoliticaDelBarbero() {
        citaRegistrada(carlos, true);

        CorteVista corte = fachada.registrarAtencion(1L, "Corte clásico", TREINTA_MIL);

        assertTrue(corte.tieneCitaPrevia());
        assertEquals(1L, corte.citaId());
        assertEquals(2L, corte.barberoId());
        assertEquals(5L, corte.clienteId());
        assertEquals("Juan", corte.clienteNombre());
        assertMonto("18000", corte.comision().montoBarbero());
        assertMonto("12000", corte.comision().montoDueno());
        verify(cortes).save(any(Corte.class));
    }

    @Test
    void cuandoCortaElDuenoAplicaLaPoliticaDelDueno() {
        citaRegistrada(oscar, true);

        CorteVista corte = fachada.registrarAtencion(1L, "Corte clásico", TREINTA_MIL);

        assertMonto("0", corte.comision().montoBarbero());
        assertMonto("30000", corte.comision().montoDueno());
    }

    @Test
    void noRegistraElCobroDeUnaCitaNoAtendida() {
        citaRegistrada(carlos, false);
        assertThrows(ReglaNegocioException.class, () -> fachada.registrarAtencion(1L, "Corte", TREINTA_MIL));
        verify(cortes, never()).save(any());
    }

    @Test
    void noRegistraDosCortesParaLaMismaCita() {
        citaRegistrada(carlos, true);
        when(cortes.existsByCitaId(1L)).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> fachada.registrarAtencion(1L, "Corte", TREINTA_MIL));
        verify(cortes, never()).save(any());
    }

    // --- HU-28 + HU-13 ---

    @Test
    void registraCorteSinCitaConClienteRegistrado() {
        when(barberos.findById(2L)).thenReturn(Optional.of(carlos));
        when(clientes.buscarCliente(5L)).thenReturn(Optional.of(juan));

        CorteVista corte = fachada.registrarAtencionSinCita(2L, new DatosCliente(5L, null, null),
                "Barba", TREINTA_MIL);

        assertFalse(corte.tieneCitaPrevia());
        assertNull(corte.citaId());
        assertEquals(DIA, corte.fecha());
        assertEquals(5L, corte.clienteId());
        assertMonto("18000", corte.comision().montoBarbero());
        verify(clientes, never()).save(any());
    }

    @Test
    void registraAlClienteNuevoQueLlegaSinCita() {
        when(barberos.findById(2L)).thenReturn(Optional.of(carlos));
        when(clientes.save(any(Cliente.class))).thenAnswer(invocacion -> conId(invocacion.getArgument(0), 9));

        CorteVista corte = fachada.registrarAtencionSinCita(2L, new DatosCliente(null, " Pedro ", "3001234567"),
                "Corte", TREINTA_MIL);

        assertEquals(9L, corte.clienteId());
        assertEquals("Pedro", corte.clienteNombre());
        verify(clientes).save(any(Cliente.class));
    }

    @Test
    void corteSinCitaExigeDatosDelCliente() {
        when(barberos.findById(2L)).thenReturn(Optional.of(carlos));

        assertThrows(IllegalArgumentException.class,
                () -> fachada.registrarAtencionSinCita(2L, new DatosCliente(null, " ", null), "Corte", TREINTA_MIL));
        assertThrows(IllegalArgumentException.class,
                () -> fachada.registrarAtencionSinCita(2L, null, "Corte", TREINTA_MIL));
        verify(cortes, never()).save(any());
    }

    @Test
    void corteSinCitaRechazaBarberoOClienteInexistente() {
        when(barberos.findById(98L)).thenReturn(Optional.empty());
        when(barberos.findById(2L)).thenReturn(Optional.of(carlos));
        when(clientes.buscarCliente(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> fachada.registrarAtencionSinCita(
                98L, new DatosCliente(5L, null, null), "Corte", TREINTA_MIL));
        assertThrows(RecursoNoEncontradoException.class, () -> fachada.registrarAtencionSinCita(
                2L, new DatosCliente(99L, null, null), "Corte", TREINTA_MIL));
    }
}
