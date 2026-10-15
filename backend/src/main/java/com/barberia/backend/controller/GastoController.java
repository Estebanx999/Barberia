package com.barberia.backend.controller;

import com.barberia.backend.domain.Dueno;
import com.barberia.backend.domain.Gasto;
import com.barberia.backend.dto.RegistrarGastoRequest;
import com.barberia.backend.repository.BarberoRepository;
import com.barberia.backend.service.GastoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** HU-14. */
@RestController
@RequestMapping("/api/gastos")
public class GastoController {

    private final GastoService gastoService;
    private final BarberoRepository barberoRepository;

    public GastoController(GastoService gastoService, BarberoRepository barberoRepository) {
        this.gastoService = gastoService;
        this.barberoRepository = barberoRepository;
    }

    @PostMapping
    public Gasto registrar(@RequestBody RegistrarGastoRequest request) {
        Dueno dueno = (Dueno) barberoRepository.findById(request.duenoId())
                .orElseThrow(() -> new IllegalArgumentException("Dueno no encontrado"));
        return gastoService.registrarGasto(dueno, request.monto(), request.categoria(), request.descripcion());
    }
}
