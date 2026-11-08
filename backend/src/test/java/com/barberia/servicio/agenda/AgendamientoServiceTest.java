package com.barberia.servicio.agenda;

import com.barberia.dominio.Barbero;
import com.barberia.dominio.Cita;
import com.barberia.dominio.EstadoCita;
import com.barberia.dominio.Servicio;
import com.barberia.repositorio.BarberoRepository;
import com.barberia.repositorio.CitaRepository;
import com.barberia.repositorio.ClienteRepository;
import com.barberia.servicio.RecursoNoEncontradoException;
import com.barberia.servicio.ReglaNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static com.barberia.soporte.DatosDePrueba.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AgendamientoServiceTest {

    private final CitaRepository citas = mock(CitaRepository.class);
    private final ClienteRepository clientes = mock(ClienteRepository.class);
    private final BarberoRepository barberos = mock(BarberoRepository.class);
    private final Barbero carlos = barbero(2, "Carlos");

    private AgendamientoService servicio;

    @BeforeEach
    void preparar() {
        Clock reloj = relojEn(DIA, LocalTime.of(8, 0));
        servicio = new AgendamientoService(citas, clientes, barberos,
                new CalculadoraDisponibilidad(HORARIO, reloj), HORARIO, reloj);

        when(clientes.buscarCliente(1L)).thenReturn(Optional.of(cliente(1, "Juan")));
        when(barberos.findByIdParaAgendar(2L)).thenReturn(Optional.of(carlos));
        when(citas.save(any(Cita.class))).thenAnswer(invocacion -> conId(invocacion.getArgument(0), 10));
    }

    private SolicitudCita solicitud(String hora) {
        return solicitud(hora, Servicio.CORTE);
    }

    private SolicitudCita solicitud(String hora, Servicio servicioPedido) {
        return new SolicitudCita(1L, 2L, servicioPedido, DIA, LocalTime.parse(hora));
    }

    @Test
    void agendaEnHorarioLibreConLaDuracionDelServicio() {
        when(citas.findByBarberoIdAndFecha(2L, DIA)).thenReturn(List.of());

        CitaVista cita = servicio.agendar(solicitud("10:00"));

        assertEquals(10L, cita.id());
        assertEquals(LocalTime.of(10, 30), cita.horaFin());
        assertEquals(EstadoCita.AGENDADA, cita.estado());
        assertEquals("Juan", cita.clienteNombre());
        assertEquals("Carlos", cita.barberoNombre());
        assertEquals(Servicio.CORTE, cita.servicio());
        verify(citas).save(any(Cita.class));
    }

    @Test
    void corteYBarbaOcupaUnaHora() {
        when(citas.findByBarberoIdAndFecha(2L, DIA)).thenReturn(List.of());

        CitaVista cita = servicio.agendar(solicitud("10:00", Servicio.CORTE_Y_BARBA));

        assertEquals(LocalTime.of(11, 0), cita.horaFin());
        assertEquals("Corte y barba", cita.servicioNombre());
    }

    @Test
    void rechazaUnServicioLargoQueAlcanzaLaSiguienteCita() {
        when(citas.findByBarberoIdAndFecha(2L, DIA)).thenReturn(List.of(cita(3, carlos, 7, "10:30")));

        var error = assertThrows(HorarioNoDisponibleException.class,
                () -> servicio.agendar(solicitud("10:00", Servicio.CORTE_Y_BARBA)));

        assertTrue(error.getHorariosDisponibles().stream()
                .noneMatch(f -> f.horaInicio().equals(LocalTime.of(10, 0))));
        verify(citas, never()).save(any());
    }

    @Test
    void rechazaUnServicioQueTerminaDespuesDelCierre() {
        assertThrowsExactly(ReglaNegocioException.class,
                () -> servicio.agendar(solicitud("19:30", Servicio.CORTE_Y_BARBA)));
        verify(citas, never()).save(any());
    }

    @Test
    void rechazaElCruceYDevuelveLosHorariosRestantes() {
        when(citas.findByBarberoIdAndFecha(2L, DIA)).thenReturn(List.of(cita(3, carlos, 7, "10:00")));

        var error = assertThrows(HorarioNoDisponibleException.class, () -> servicio.agendar(solicitud("10:15")));

        assertFalse(error.getHorariosDisponibles().isEmpty());
        assertTrue(error.getHorariosDisponibles().stream()
                .noneMatch(f -> f.horaInicio().equals(LocalTime.of(10, 0))));
        verify(citas, never()).save(any());
    }

    @Test
    void aceptaUnaCitaContiguaAOtra() {
        when(citas.findByBarberoIdAndFecha(2L, DIA)).thenReturn(List.of(cita(3, carlos, 7, "10:00")));
        assertEquals(LocalTime.of(10, 30), servicio.agendar(solicitud("10:30")).horaInicio());
    }

    @Test
    void rechazaClienteOBarberoInexistente() {
        when(clientes.buscarCliente(99L)).thenReturn(Optional.empty());
        when(barberos.findByIdParaAgendar(98L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.agendar(new SolicitudCita(99L, 2L, Servicio.CORTE, DIA, LocalTime.of(10, 0))));
        assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.agendar(new SolicitudCita(1L, 98L, Servicio.CORTE, DIA, LocalTime.of(10, 0))));
    }

    @Test
    void rechazaHorariosFueraDeLaJornada() {
        assertThrowsExactly(ReglaNegocioException.class, () -> servicio.agendar(solicitud("07:30")));
        assertThrowsExactly(ReglaNegocioException.class, () -> servicio.agendar(solicitud("19:45")));
        verify(citas, never()).save(any());
    }

    @Test
    void rechazaFechasPasadas() {
        var ayer = new SolicitudCita(1L, 2L, Servicio.CORTE, DIA.minusDays(1), LocalTime.of(10, 0));
        assertThrowsExactly(ReglaNegocioException.class, () -> servicio.agendar(ayer));
        verify(citas, never()).save(any());
    }
}
