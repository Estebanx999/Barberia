package com.barberia.dominio;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Barbero que atiende cortes. En el MVP solo expone esDueno(), la pregunta
 * que usa AtencionFacade para elegir la política de comisión.
 */
@Entity
@DiscriminatorValue("BARBERO")
public class Barbero extends Usuario {

    protected Barbero() {
        // requerido por JPA
    }

    public Barbero(String nombre, String correo, String telefono) {
        super(nombre, correo, telefono);
    }

    public boolean esDueno() {
        return false;
    }
}
