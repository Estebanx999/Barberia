package com.barberia.backend.controller;

import com.barberia.backend.domain.Barbero;
import com.barberia.backend.repository.BarberoRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Soporte a HU-01 (seleccionar barbero) y HU-09/HU-10 (listar equipo). */
@RestController
@RequestMapping("/api/barberos")
public class BarberoController {

    private final BarberoRepository barberoRepository;

    public BarberoController(BarberoRepository barberoRepository) {
        this.barberoRepository = barberoRepository;
    }

    @GetMapping
    public List<Barbero> listar() {
        return barberoRepository.findByActivoTrue();
    }
}
