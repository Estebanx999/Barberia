package com.barberia.servicio.autenticacion;

import com.barberia.dominio.Usuario;

/**
 * Emite el token de acceso de un usuario autenticado. El servicio depende de
 * esta abstracción y no del formato del token (DIP); la implementación está
 * en la capa de seguridad.
 */
public interface EmisorToken {

    String emitir(Usuario usuario);
}
