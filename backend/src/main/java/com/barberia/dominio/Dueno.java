package com.barberia.dominio;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * El dueño también corta (RN-02). Hereda de Barbero y solo sobrescribe esDueno();
 * no restringe ningún comportamiento heredado (LSP). Registrar gastos y ver
 * reportes viven en la capa de servicios, no aquí.
 */
@Entity
@DiscriminatorValue("DUENO")
public class Dueno extends Barbero {

    protected Dueno() {
        // requerido por JPA
    }

    public Dueno(String nombre, String correo, String telefono) {
        super(nombre, correo, telefono);
    }

    @Override
    public boolean esDueno() {
        return true;
    }

    @Override
    public Rol getRol() {
        return Rol.DUENO;
    }
}
