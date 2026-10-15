package com.barberia.backend.domain.observer;

import com.barberia.backend.domain.Cita;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observador concreto: simula el envio de una notificacion/recordatorio
 * (HU-08: recibir confirmacion de cita) cuando el estado de la Cita
 * cambia. En una iteracion futura (Release 2 del story map) este mismo
 * observador se reemplaza o complementa con un envio real de correo/SMS,
 * sin tocar la clase Cita.
 */
@Component
public class NotificacionObservador implements ObservadorCita {

    private static final Logger log = LoggerFactory.getLogger(NotificacionObservador.class);

    @Override
    public void actualizar(Object citaObj) {
        if (citaObj instanceof Cita cita) {
            log.info("Notificacion -> cliente {}: tu cita con {} el {} a las {} ahora esta {}",
                    cita.getCliente().getNombre(),
                    cita.getBarbero().getNombre(),
                    cita.getFecha(),
                    cita.getHoraInicio(),
                    cita.getEstado());
        }
    }
}
