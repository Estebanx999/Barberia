package com.barberia.backend.domain.observer;

import com.barberia.backend.domain.Cita;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observador concreto: mantiene actualizada la vista de "calendario del
 * dia" del barbero (HU-05, HU-06) cada vez que una Cita cambia de estado,
 * sin que la entidad Cita conozca los detalles de esa vista.
 */
@Component
public class AgendaBarberoObservador implements ObservadorCita {

    private static final Logger log = LoggerFactory.getLogger(AgendaBarberoObservador.class);

    @Override
    public void actualizar(Object citaObj) {
        if (citaObj instanceof Cita cita) {
            log.info("Agenda de {} actualizada: cita {} -> {}",
                    cita.getBarbero().getNombre(), cita.getId(), cita.getEstado());
        }
    }
}
