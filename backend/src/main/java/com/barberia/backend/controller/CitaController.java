package com.barberia.backend.controller;

import com.barberia.backend.domain.Cita;
import com.barberia.backend.domain.Cliente;
import com.barberia.backend.dto.AgendarCitaRequest;
import com.barberia.backend.repository.ClienteRepository;
import com.barberia.backend.service.CitaService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** HU-01, HU-02, HU-05, HU-06, HU-07, HU-08. */
@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;
    private final ClienteRepository clienteRepository;

    public CitaController(CitaService citaService, ClienteRepository clienteRepository) {
        this.citaService = citaService;
        this.clienteRepository = clienteRepository;
    }

    @GetMapping("/barbero/{barberoId}")
    public List<Cita> citasDelDia(@PathVariable Long barberoId,
                                   @RequestParam("fecha") String fecha) {
        return citaService.consultarCitasDelDia(barberoId, LocalDate.parse(fecha));
    }

    @PostMapping
    public Cita agendar(@RequestBody AgendarCitaRequest request) {
        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        return citaService.agendarCita(cliente, request.barberoId(), request.fecha(), request.horaInicio(), request.horaFin());
    }
}
