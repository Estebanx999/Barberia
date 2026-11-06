package com.barberia.servicio.agenda;

import com.barberia.servicio.ReglaNegocioException;

import java.util.List;

/** RN-04: el horario pedido se cruza con otra cita del mismo barbero. */
public class HorarioNoDisponibleException extends ReglaNegocioException {

    private final List<Franja> horariosDisponibles;

    public HorarioNoDisponibleException(List<Franja> horariosDisponibles) {
        super("El horario seleccionado no está disponible para este barbero");
        this.horariosDisponibles = List.copyOf(horariosDisponibles);
    }

    public List<Franja> getHorariosDisponibles() {
        return horariosDisponibles;
    }
}
