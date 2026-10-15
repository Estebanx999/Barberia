package com.barberia.backend.service;

import com.barberia.backend.domain.Barbero;
import com.barberia.backend.domain.Cliente;
import com.barberia.backend.domain.Corte;
import com.barberia.backend.repository.BarberoRepository;
import com.barberia.backend.repository.CorteRepository;
import org.springframework.stereotype.Service;

/**
 * Implementa HU-12 (Registrar corte realizado) y HU-13 (Calcular
 * comision automatica por corte). El calculo delega en el patron
 * STRATEGY asignado al barbero (ver Barbero.calcularComision).
 */
@Service
public class CorteService {

    private final CorteRepository corteRepository;
    private final BarberoRepository barberoRepository;

    public CorteService(CorteRepository corteRepository, BarberoRepository barberoRepository) {
        this.corteRepository = corteRepository;
        this.barberoRepository = barberoRepository;
    }

    public Corte registrarCorte(Long barberoId, Cliente cliente, double valorCobrado, boolean tieneCitaPrevia) {
        Barbero barbero = barberoRepository.findById(barberoId)
                .orElseThrow(() -> new IllegalArgumentException("Barbero no encontrado"));

        Corte corte = new Corte();
        corte.setBarbero(barbero);
        corte.setCliente(cliente);
        corte.setValorCobrado(valorCobrado);
        corte.setTieneCitaPrevia(tieneCitaPrevia);
        corte.generarComision(); // HU-13: Strategy

        return corteRepository.save(corte);
    }
}
