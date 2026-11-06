package com.barberia.servicio.autenticacion;

import com.barberia.dominio.Rol;
import com.barberia.dominio.Usuario;

/** Datos públicos del usuario; nunca incluye la contraseña. */
public record UsuarioVista(Long id, String nombre, String correo, String telefono, Rol rol) {

    public static UsuarioVista de(Usuario usuario) {
        return new UsuarioVista(usuario.getId(), usuario.getNombre(), usuario.getCorreo(),
                usuario.getTelefono(), usuario.getRol());
    }
}
