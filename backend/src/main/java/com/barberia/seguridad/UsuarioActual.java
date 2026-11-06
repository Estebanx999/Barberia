package com.barberia.seguridad;

import com.barberia.dominio.Rol;
import com.barberia.servicio.AccesoDenegadoException;

/**
 * Usuario autenticado en la solicitud en curso, tomado del token. Los
 * controladores lo reciben como parámetro para no depender del formato del token.
 */
public record UsuarioActual(Long id, Rol rol) {

    /** HU-26: un barbero solo ve su propia información; el dueño ve la de todos. */
    public void verificarAccesoABarbero(Long barberoId) {
        boolean esElMismo = id.equals(barberoId);
        if (rol != Rol.DUENO && !esElMismo) {
            throw new AccesoDenegadoException("Solo puede consultar su propia información");
        }
    }
}
