package com.barberia.servicio.agenda;

import com.barberia.dominio.Cita;
import com.barberia.dominio.Servicio;
import com.barberia.dominio.Usuario;
import com.barberia.repositorio.BarberoRepository;
import com.barberia.repositorio.CitaRepository;
import com.barberia.repositorio.ClienteRepository;
import com.barberia.servicio.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Consultas de la agenda: barberos y servicios para elegir, franjas libres
 * (HU-02), calendario de un barbero (HU-05) y calendario general (HU-06).
 * Solo lee; el agendamiento está en AgendamientoService.
 */
@Service
@Transactional(readOnly = true)
public class ConsultaAgendaService {

    private final CitaRepository citas;
    private final BarberoRepository barberos;
    private final ClienteRepository clientes;
    private final CalculadoraDisponibilidad disponibilidad;

    public ConsultaAgendaService(CitaRepository citas,
                                 BarberoRepository barberos,
                                 ClienteRepository clientes,
                                 CalculadoraDisponibilidad disponibilidad) {
        this.citas = citas;
        this.barberos = barberos;
        this.clientes = clientes;
        this.disponibilidad = disponibilidad;
    }

    public List<BarberoVista> listarBarberos() {
        return barberos.findAllByOrderByNombre().stream()
                .map(BarberoVista::de)
                .toList();
    }

    /** Servicios que se pueden reservar, con el tiempo que ocupa cada uno. */
    public List<ServicioVista> listarServicios() {
        return Arrays.stream(Servicio.values()).map(ServicioVista::de).toList();
    }

    /** HU-02: franjas del día en las que el servicio cabe sin cruzarse con las citas del barbero. */
    public List<Franja> horariosDisponibles(Long barberoId, LocalDate fecha, Servicio servicio) {
        verificarBarbero(barberoId);
        return disponibilidad.franjasLibres(
                fecha, citas.findByBarberoIdAndFecha(barberoId, fecha), servicio.getDuracion());
    }

    /** HU-05: citas del barbero en el día, ordenadas por hora. */
    public List<CitaVista> calendarioBarbero(Long barberoId, LocalDate fecha) {
        verificarBarbero(barberoId);
        return conNombresDeClientes(citas.findByBarberoIdAndFechaOrderByHoraInicio(barberoId, fecha));
    }

    /** HU-24: citas del cliente autenticado, las más recientes primero. */
    public List<CitaVista> citasDelCliente(Long clienteId) {
        return conNombresDeClientes(citas.findByClienteIdOrderByFechaDescHoraInicioDesc(clienteId));
    }

    /** HU-06: citas de todos los barberos en el día, agrupadas por barbero y ordenadas por hora. */
    public List<CalendarioBarbero> calendarioGeneral(LocalDate fecha) {
        Map<Long, List<CitaVista>> citasPorBarbero =
                conNombresDeClientes(citas.findByFechaOrderByHoraInicio(fecha)).stream()
                        .collect(Collectors.groupingBy(CitaVista::barberoId));

        return barberos.findAllByOrderByNombre().stream()
                .map(barbero -> new CalendarioBarbero(
                        barbero.getId(),
                        barbero.getNombre(),
                        citasPorBarbero.getOrDefault(barbero.getId(), List.of())))
                .toList();
    }

    private void verificarBarbero(Long barberoId) {
        if (!barberos.existsById(barberoId)) {
            throw new RecursoNoEncontradoException("Barbero", barberoId);
        }
    }

    /** Resuelve los nombres de los clientes en una sola consulta (Cita solo guarda clienteId). */
    private List<CitaVista> conNombresDeClientes(List<Cita> lista) {
        Set<Long> ids = lista.stream().map(Cita::getClienteId).collect(Collectors.toSet());
        Map<Long, String> nombres = clientes.findAllById(ids).stream()
                .collect(Collectors.toMap(Usuario::getId, Usuario::getNombre));
        return lista.stream()
                .map(cita -> CitaVista.de(cita, nombres.get(cita.getClienteId())))
                .toList();
    }
}
