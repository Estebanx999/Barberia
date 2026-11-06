package com.barberia.web;

import com.barberia.reporte.Periodo;
import com.barberia.seguridad.UsuarioActual;
import com.barberia.servicio.agenda.CitaVista;
import com.barberia.servicio.atencion.AtencionFacade;
import com.barberia.servicio.atencion.ConsultaCortesService;
import com.barberia.servicio.atencion.ConsultaCortesService.ResumenCortes;
import com.barberia.servicio.atencion.CorteVista;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Flujo de atención: los registros delegan en AtencionFacade. El barbero que
 * atiende es siempre el usuario autenticado (el dueño también corta).
 */
@RestController
@RequestMapping("/api")
public class AtencionController {

    private final AtencionFacade atencion;
    private final ConsultaCortesService consultaCortes;

    public AtencionController(AtencionFacade atencion, ConsultaCortesService consultaCortes) {
        this.atencion = atencion;
        this.consultaCortes = consultaCortes;
    }

    /** HU-12: marca la cita como atendida. */
    @PatchMapping("/citas/{citaId}/atendida")
    public CitaVista marcarAtendida(UsuarioActual usuario, @PathVariable Long citaId) {
        return atencion.marcarAtendida(citaId, usuario.id());
    }

    /** HU-27 + HU-13: registra el cobro de la cita atendida y calcula la comisión. */
    @PostMapping("/citas/{citaId}/corte")
    @ResponseStatus(HttpStatus.CREATED)
    public CorteVista registrarCobro(UsuarioActual usuario, @PathVariable Long citaId,
                                     @Valid @RequestBody CobroRequest cobro) {
        return atencion.registrarAtencion(citaId, usuario.id(), cobro.servicio(), cobro.valorCobrado());
    }

    /** HU-28 + HU-13: registra un corte sin cita previa y calcula la comisión. */
    @PostMapping("/cortes")
    @ResponseStatus(HttpStatus.CREATED)
    public CorteVista registrarSinCita(UsuarioActual usuario, @Valid @RequestBody CorteSinCitaRequest solicitud) {
        return atencion.registrarAtencionSinCita(usuario.id(), solicitud.datosCliente(),
                solicitud.servicio(), solicitud.valorCobrado());
    }

    /** HU-23 / HU-26: cortes y comisiones del barbero autenticado en el periodo. */
    @GetMapping("/cortes/mios")
    public ResumenCortes misCortes(
            UsuarioActual usuario,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return consultaCortes.cortesDelBarbero(usuario.id(), new Periodo(desde, hasta));
    }
}
