package com.barberia.backend.service;

import com.barberia.backend.domain.Dueno;
import com.barberia.backend.domain.Gasto;
import com.barberia.backend.repository.GastoRepository;
import org.springframework.stereotype.Service;

/** Implementa HU-14 (Registrar gasto operativo). */
@Service
public class GastoService {

    private final GastoRepository gastoRepository;

    public GastoService(GastoRepository gastoRepository) {
        this.gastoRepository = gastoRepository;
    }

    public Gasto registrarGasto(Dueno dueno, double monto, String categoria, String descripcion) {
        Gasto gasto = new Gasto();
        gasto.setRegistradoPor(dueno);
        gasto.setMonto(monto);
        gasto.setCategoria(categoria);
        gasto.setDescripcion(descripcion);
        return gastoRepository.save(gasto);
    }
}
