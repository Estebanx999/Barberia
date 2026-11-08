package com.barberia.servicio.agenda;

import com.barberia.dominio.Barbero;
import com.barberia.dominio.Cita;
import com.barberia.dominio.Cliente;
import com.barberia.repositorio.BarberoRepository;
import com.barberia.repositorio.CitaRepository;
import com.barberia.repositorio.ClienteRepository;
import com.barberia.servicio.RecursoNoEncontradoException;
import com.barberia.servicio.ReglaNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Caso de uso "agendar cita" (HU-01) con validación de cruce (HU-07, RN-04).
 * Antes vivía en Cliente.agendarCita; se movió a la capa de servicios para que
 * la entidad no mezcle datos con el caso de uso (SRP).
 */
@Service
public class AgendamientoService {

    private final CitaRepository citas;
    private final ClienteRepository clientes;
    private final BarberoRepository barberos;
    private final CalculadoraDisponibilidad disponibilidad;
    private final HorarioAtencion horario;
    private final Clock reloj;

    public AgendamientoService(CitaRepository citas,
                               ClienteRepository clientes,
                               BarberoRepository barberos,
                               CalculadoraDisponibilidad disponibilidad,
                               HorarioAtencion horario,
                               Clock reloj) {
        this.citas = citas;
        this.clientes = clientes;
        this.barberos = barberos;
        this.disponibilidad = disponibilidad;
        this.horario = horario;
        this.reloj = reloj;
    }

    @Transactional
    public CitaVista agendar(SolicitudCita solicitud) {
        Cliente cliente = clientes.buscarCliente(solicitud.clienteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", solicitud.clienteId()));
        Barbero barbero = barberos.findByIdParaAgendar(solicitud.barberoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Barbero", solicitud.barberoId()));

        validarHorario(solicitud);

        Cita nueva = new Cita(solicitud.fecha(), solicitud.horaInicio(), solicitud.servicio(),
                cliente.getId(), barbero);

        List<Cita> citasDelDia = citas.findByBarberoIdAndFecha(barbero.getId(), solicitud.fecha());
        if (citasDelDia.stream().anyMatch(nueva::seCruzaCon)) {
            throw new HorarioNoDisponibleException(disponibilidad.franjasLibres(
                    solicitud.fecha(), citasDelDia, solicitud.servicio().getDuracion()));
        }

        return CitaVista.de(citas.save(nueva), cliente.getNombre());
    }

    private void validarHorario(SolicitudCita solicitud) {
        if (solicitud.servicio() == null) {
            throw new IllegalArgumentException("El servicio es obligatorio");
        }
        if (!horario.cabeEnJornada(solicitud.horaInicio(), solicitud.servicio().getDuracion())) {
            throw new ReglaNegocioException("El horario está fuera de la jornada de atención ("
                    + horario.apertura() + " a " + horario.cierre() + ")");
        }
        LocalDateTime inicio = LocalDateTime.of(solicitud.fecha(), solicitud.horaInicio());
        if (inicio.isBefore(LocalDateTime.now(reloj))) {
            throw new ReglaNegocioException("No se puede agendar una cita en una fecha u hora pasada");
        }
    }
}
