package com.barberia.backend.controller;

import com.barberia.backend.domain.Cliente;
import com.barberia.backend.domain.Corte;
import com.barberia.backend.dto.RegistrarCorteRequest;
import com.barberia.backend.repository.ClienteRepository;
import com.barberia.backend.service.CorteService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** HU-12, HU-13. */
@RestController
@RequestMapping("/api/cortes")
public class CorteController {

    private final CorteService corteService;
    private final ClienteRepository clienteRepository;

    public CorteController(CorteService corteService, ClienteRepository clienteRepository) {
        this.corteService = corteService;
        this.clienteRepository = clienteRepository;
    }

    @PostMapping
    public Corte registrar(@RequestBody RegistrarCorteRequest request) {
        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        return corteService.registrarCorte(request.barberoId(), cliente, request.valorCobrado(), request.tieneCitaPrevia());
    }
}
