package com.barberia.web;

import com.barberia.servicio.agenda.CitaVista;
import com.barberia.servicio.atencion.AtencionFacade;
import com.barberia.servicio.atencion.CorteVista;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** Flujo de atención: todos los endpoints delegan en AtencionFacade. */
@RestController
@RequestMapping("/api")
public class AtencionController {

    private final AtencionFacade atencion;

    public AtencionController(AtencionFacade atencion) {
        this.atencion = atencion;
    }

    /** HU-12: marca la cita como atendida. */
    @PatchMapping("/citas/{citaId}/atendida")
    public CitaVista marcarAtendida(@PathVariable Long citaId) {
        return atencion.marcarAtendida(citaId);
    }

    /** HU-27 + HU-13: registra el cobro de la cita atendida y calcula la comisión. */
    @PostMapping("/citas/{citaId}/corte")
    @ResponseStatus(HttpStatus.CREATED)
    public CorteVista registrarCobro(@PathVariable Long citaId, @Valid @RequestBody CobroRequest cobro) {
        return atencion.registrarAtencion(citaId, cobro.servicio(), cobro.valorCobrado());
    }

    /** HU-28 + HU-13: registra un corte sin cita previa y calcula la comisión. */
    @PostMapping("/cortes")
    @ResponseStatus(HttpStatus.CREATED)
    public CorteVista registrarSinCita(@Valid @RequestBody CorteSinCitaRequest solicitud) {
        return atencion.registrarAtencionSinCita(solicitud.barberoId(), solicitud.datosCliente(),
                solicitud.servicio(), solicitud.valorCobrado());
    }
}
