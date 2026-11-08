package com.barberia.servicio.agenda;

import com.barberia.dominio.Barbero;
import com.barberia.dominio.Servicio;
import com.barberia.repositorio.BarberoRepository;
import com.barberia.repositorio.CitaRepository;
import com.barberia.repositorio.ClienteRepository;
import com.barberia.servicio.RecursoNoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static com.barberia.soporte.DatosDePrueba.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConsultaAgendaServiceTest {

    private final CitaRepository citas = mock(CitaRepository.class);
    private final BarberoRepository barberos = mock(BarberoRepository.class);
    private final ClienteRepository clientes = mock(ClienteRepository.class);
    private final Barbero andres = barbero(1, "Andrés");
    private final Barbero carlos = barbero(2, "Carlos");

    private ConsultaAgendaService servicio;

    @BeforeEach
    void preparar() {
        var calculadora = new CalculadoraDisponibilidad(HORARIO, relojEn(DIA, LocalTime.of(8, 0)));
        servicio = new ConsultaAgendaService(citas, barberos, clientes, calculadora);
        when(barberos.findAllByOrderByNombre()).thenReturn(List.of(andres, carlos));
        when(clientes.findAllById(any())).thenReturn(List.of(cliente(5, "Juan"), cliente(6, "Pedro")));
    }

    @Test
    void calendarioGeneralAgrupaPorBarberoEIncluyeLosQueNoTienenCitas() {
        when(citas.findByFechaOrderByHoraInicio(DIA)).thenReturn(List.of(
                cita(1, carlos, 5, "09:00"),
                cita(2, carlos, 6, "11:00")));

        List<CalendarioBarbero> calendario = servicio.calendarioGeneral(DIA);

        assertEquals(2, calendario.size());
        assertEquals("Andrés", calendario.get(0).barberoNombre());
        assertTrue(calendario.get(0).citas().isEmpty());
        List<CitaVista> deCarlos = calendario.get(1).citas();
        assertEquals(List.of("Juan", "Pedro"), deCarlos.stream().map(CitaVista::clienteNombre).toList());
        assertEquals(LocalTime.of(9, 0), deCarlos.get(0).horaInicio());
    }

    @Test
    void calendarioDelBarberoMuestraSusCitasConElNombreDelCliente() {
        when(barberos.existsById(2L)).thenReturn(true);
        when(citas.findByBarberoIdAndFechaOrderByHoraInicio(2L, DIA))
                .thenReturn(List.of(cita(1, carlos, 5, "09:00")));

        List<CitaVista> calendario = servicio.calendarioBarbero(2L, DIA);

        assertEquals(1, calendario.size());
        assertEquals("Juan", calendario.get(0).clienteNombre());
    }

    @Test
    void horariosDisponiblesExcluyeLasCitasDelBarbero() {
        when(barberos.existsById(2L)).thenReturn(true);
        when(citas.findByBarberoIdAndFecha(2L, DIA)).thenReturn(List.of(cita(1, carlos, 5, "09:00")));

        List<Franja> libres = servicio.horariosDisponibles(2L, DIA, Servicio.CORTE);

        assertEquals(23, libres.size());
        assertTrue(libres.stream().noneMatch(f -> f.horaInicio().equals(LocalTime.of(9, 0))));
    }

    @Test
    void barberoInexistenteResponde404() {
        when(barberos.existsById(99L)).thenReturn(false);
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.horariosDisponibles(99L, DIA, Servicio.CORTE));
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.calendarioBarbero(99L, DIA));
    }

    @Test
    void listaLosBarberosConSuPerfil() {
        carlos.actualizarPerfil("Especialista en degradados", " ");

        assertEquals(List.of(
                        new BarberoVista(1L, "Andrés", null, null),
                        new BarberoVista(2L, "Carlos", "Especialista en degradados", null)),
                servicio.listarBarberos());
    }

    @Test
    void listaLosServiciosConSuDuracion() {
        List<ServicioVista> servicios = servicio.listarServicios();

        assertEquals(List.of(Servicio.CORTE, Servicio.BARBA, Servicio.CORTE_Y_BARBA),
                servicios.stream().map(ServicioVista::codigo).toList());
        assertEquals(60, servicios.get(2).duracionMinutos());
    }
}
