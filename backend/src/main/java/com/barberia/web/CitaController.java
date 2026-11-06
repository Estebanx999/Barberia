package com.barberia.web;

import com.barberia.servicio.agenda.AgendamientoService;
import com.barberia.servicio.agenda.CalendarioBarbero;
import com.barberia.servicio.agenda.CitaVista;
import com.barberia.servicio.agenda.ConsultaAgendaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final AgendamientoService agendamiento;
    private final ConsultaAgendaService consultas;

    public CitaController(AgendamientoService agendamiento, ConsultaAgendaService consultas) {
        this.agendamiento = agendamiento;
        this.consultas = consultas;
    }

    /** HU-01 / HU-07: agenda la cita si el horario está libre. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CitaVista agendar(@Valid @RequestBody SolicitudCitaRequest solicitud) {
        return agendamiento.agendar(solicitud.aSolicitud());
    }

    /** HU-06: calendario general del día, agrupado por barbero. */
    @GetMapping
    public List<CalendarioBarbero> calendarioGeneral(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return consultas.calendarioGeneral(fecha);
    }
}
