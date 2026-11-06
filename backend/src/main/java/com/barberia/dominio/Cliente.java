package com.barberia.dominio;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Cliente de la barbería. Solo guarda datos: agendar una cita es un caso de uso
 * y vive en la capa de servicios (recomendación SRP del análisis SOLID).
 * La relación Cliente–Cita se guarda en un solo sentido: Cita.clienteId.
 */
@Entity
@DiscriminatorValue("CLIENTE")
public class Cliente extends Usuario {

    protected Cliente() {
        // requerido por JPA
    }

    public Cliente(String nombre, String correo, String telefono) {
        super(nombre, correo, telefono);
    }

    @Override
    public Rol getRol() {
        return Rol.CLIENTE;
    }
}
