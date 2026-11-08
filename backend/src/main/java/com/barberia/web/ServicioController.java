package com.barberia.web;

import com.barberia.servicio.agenda.ConsultaAgendaService;
import com.barberia.servicio.agenda.ServicioVista;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
public class ServicioController {

    private final ConsultaAgendaService consultas;

    public ServicioController(ConsultaAgendaService consultas) {
        this.consultas = consultas;
    }

    /** Servicios que el cliente puede reservar y cuánto tiempo ocupa cada uno. */
    @GetMapping
    public List<ServicioVista> listar() {
        return consultas.listarServicios();
    }
}
