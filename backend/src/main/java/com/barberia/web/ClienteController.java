package com.barberia.web;

import com.barberia.servicio.cliente.BusquedaClientesService;
import com.barberia.servicio.cliente.BusquedaClientesService.ClienteResumen;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final BusquedaClientesService busqueda;

    public ClienteController(BusquedaClientesService busqueda) {
        this.busqueda = busqueda;
    }

    /** HU-28: búsqueda por nombre o teléfono (mínimo 2 caracteres, máximo 10 resultados). */
    @GetMapping
    public List<ClienteResumen> buscar(@RequestParam(defaultValue = "") String buscar) {
        return busqueda.buscar(buscar);
    }
}
