package com.barberia.servicio.gasto;

import com.barberia.dominio.Gasto;
import com.barberia.repositorio.GastoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/** HU-14: registro de gastos operativos; quedan disponibles para los reportes del periodo. */
@Service
public class GastoService {

    private final GastoRepository gastos;

    public GastoService(GastoRepository gastos) {
        this.gastos = gastos;
    }

    @Transactional
    public GastoVista registrar(String concepto, LocalDate fecha, BigDecimal valor) {
        return GastoVista.de(gastos.save(new Gasto(concepto, fecha, valor)));
    }
}
