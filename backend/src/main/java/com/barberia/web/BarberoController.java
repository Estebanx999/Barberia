package com.barberia.web;

import com.barberia.seguridad.UsuarioActual;
import com.barberia.servicio.agenda.BarberoVista;
import com.barberia.servicio.agenda.CitaVista;
import com.barberia.servicio.agenda.ConsultaAgendaService;
import com.barberia.servicio.agenda.Franja;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/barberos")
public class BarberoController {

    private final ConsultaAgendaService consultas;

    public BarberoController(ConsultaAgendaService consultas) {
        this.consultas = consultas;
    }

    /** Barberos que el cliente puede elegir al agendar (HU-01). */
    @GetMapping
    public List<BarberoVista> listar() {
        return consultas.listarBarberos();
    }

    /** HU-02: franjas libres del barbero en el día. */
    @GetMapping("/{barberoId}/disponibilidad")
    public List<Franja> disponibilidad(
            @PathVariable Long barberoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return consultas.horariosDisponibles(barberoId, fecha);
    }

    /** HU-05: calendario del barbero en el día, ordenado por hora. HU-26: un barbero solo ve el suyo. */
    @GetMapping("/{barberoId}/citas")
    public List<CitaVista> calendario(
            UsuarioActual usuario,
            @PathVariable Long barberoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        usuario.verificarAccesoABarbero(barberoId);
        return consultas.calendarioBarbero(barberoId, fecha);
    }
}
