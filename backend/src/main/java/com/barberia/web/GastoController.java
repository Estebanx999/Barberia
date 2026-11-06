package com.barberia.web;

import com.barberia.servicio.gasto.GastoService;
import com.barberia.servicio.gasto.GastoVista;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gastos")
public class GastoController {

    private final GastoService gastos;

    public GastoController(GastoService gastos) {
        this.gastos = gastos;
    }

    /** HU-14: registra un gasto operativo. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GastoVista registrar(@Valid @RequestBody GastoRequest gasto) {
        return gastos.registrar(gasto.concepto(), gasto.fecha(), gasto.valor());
    }
}
