package com.barberia.servicio.atencion;

import com.barberia.dominio.*;
import com.barberia.politica.NombresPolitica;
import com.barberia.politica.PoliticaComision;
import com.barberia.repositorio.BarberoRepository;
import com.barberia.repositorio.CitaRepository;
import com.barberia.repositorio.ClienteRepository;
import com.barberia.repositorio.CorteRepository;
import com.barberia.servicio.RecursoNoEncontradoException;
import com.barberia.servicio.ReglaNegocioException;
import com.barberia.servicio.agenda.CitaVista;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

/**
 * Patrón Facade: punto único de entrada al flujo de atención. Coordina la
 * cita, el corte y el cálculo de la comisión, que antes recaía en
 * Barbero.registrarCorte(). Alcance: HU-12, HU-27 y HU-28, con HU-13.
 */
@Service
@Transactional
public class AtencionFacade {

    private final CitaRepository citas;
    private final CorteRepository cortes;
    private final BarberoRepository barberos;
    private final ClienteRepository clientes;
    private final PoliticaComision politicaBarbero;
    private final PoliticaComision politicaDueno;
    private final Clock reloj;

    public AtencionFacade(CitaRepository citas,
                          CorteRepository cortes,
                          BarberoRepository barberos,
                          ClienteRepository clientes,
                          @Qualifier(NombresPolitica.BARBERO) PoliticaComision politicaBarbero,
                          @Qualifier(NombresPolitica.DUENO) PoliticaComision politicaDueno,
                          Clock reloj) {
        this.citas = citas;
        this.cortes = cortes;
        this.barberos = barberos;
        this.clientes = clientes;
        this.politicaBarbero = politicaBarbero;
        this.politicaDueno = politicaDueno;
        this.reloj = reloj;
    }

    /** HU-12: marca la cita como atendida y habilita el registro del corte. */
    public CitaVista marcarAtendida(Long citaId) {
        Cita cita = buscarCitaParaAtender(citaId);
        if (cita.getFecha().isAfter(hoy())) {
            throw new ReglaNegocioException("Solo se puede marcar como atendida una cita de hoy o de días anteriores");
        }
        if (cita.getEstado() == EstadoCita.ATENDIDA) {
            throw new ReglaNegocioException("La cita ya fue marcada como atendida");
        }
        cita.marcarAtendida();
        return CitaVista.de(cita, nombreDelCliente(cita.getClienteId()));
    }

    /** HU-27 + HU-13: registra el cobro de una cita atendida y calcula la comisión. */
    public CorteVista registrarAtencion(Long citaId, String servicio, BigDecimal valorCobrado) {
        Cita cita = buscarCitaParaAtender(citaId);
        if (cita.getEstado() != EstadoCita.ATENDIDA) {
            throw new ReglaNegocioException("La cita debe estar marcada como atendida antes de registrar el cobro");
        }
        if (cortes.existsByCitaId(citaId)) {
            throw new ReglaNegocioException("La cita ya tiene un corte registrado");
        }
        Corte corte = Corte.deCita(cita, servicio, valorCobrado);
        return guardarConComision(corte, nombreDelCliente(cita.getClienteId()));
    }

    /**
     * HU-28 + HU-13: registra el corte de un cliente sin cita previa. Si el
     * cliente no está registrado, se registra con los datos recibidos.
     */
    public CorteVista registrarAtencionSinCita(Long barberoId, DatosCliente datosCliente,
                                               String servicio, BigDecimal valorCobrado) {
        Barbero barbero = barberos.findById(barberoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Barbero", barberoId));
        Cliente cliente = obtenerORegistrarCliente(datosCliente);
        Corte corte = Corte.sinCita(hoy(), servicio, valorCobrado, barbero, cliente.getId());
        return guardarConComision(corte, cliente.getNombre());
    }

    private CorteVista guardarConComision(Corte corte, String clienteNombre) {
        Comision comision = politicaPara(corte.getBarbero()).calcular(corte.getValorCobrado());
        corte.asignarComision(comision);
        return CorteVista.de(cortes.save(corte), clienteNombre);
    }

    /** RN-01 / RN-02: la selección de la estrategia está centralizada en un solo punto. */
    private PoliticaComision politicaPara(Barbero barbero) {
        return barbero.esDueno() ? politicaDueno : politicaBarbero;
    }

    private Cliente obtenerORegistrarCliente(DatosCliente datos) {
        if (datos == null || !datos.estaIndicado()) {
            throw new IllegalArgumentException("Indique el cliente registrado o el nombre del cliente nuevo");
        }
        if (datos.id() != null) {
            return clientes.buscarCliente(datos.id())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", datos.id()));
        }
        return clientes.save(new Cliente(datos.nombre().trim(), null, datos.telefono()));
    }

    private Cita buscarCitaParaAtender(Long citaId) {
        return citas.findByIdParaAtender(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita", citaId));
    }

    private String nombreDelCliente(Long clienteId) {
        return clientes.findById(clienteId).map(Usuario::getNombre).orElse(null);
    }

    private LocalDate hoy() {
        return LocalDate.now(reloj);
    }
}
