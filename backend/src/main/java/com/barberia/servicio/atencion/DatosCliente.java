package com.barberia.servicio.atencion;

/**
 * Cliente de un corte sin cita (HU-28): el id de un cliente registrado, o el
 * nombre (y teléfono opcional) de uno nuevo.
 */
public record DatosCliente(Long id, String nombre, String telefono) {

    public boolean estaIndicado() {
        return id != null || (nombre != null && !nombre.isBlank());
    }
}
