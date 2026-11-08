package com.barberia.servicio.agenda;

import com.barberia.dominio.Barbero;

/**
 * Perfil del barbero que el cliente ve al agendar (HU-01): nombre, descripción
 * y foto. No incluye el rol, que no le interesa al cliente.
 */
public record BarberoVista(Long id, String nombre, String descripcion, String fotoUrl) {

    public static BarberoVista de(Barbero barbero) {
        return new BarberoVista(barbero.getId(), barbero.getNombre(),
                barbero.getDescripcion(), barbero.getFotoUrl());
    }
}
