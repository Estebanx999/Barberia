package com.barberia.servicio.autenticacion;

/** Respuesta del inicio de sesión: el token para las siguientes solicitudes y el usuario. */
public record SesionIniciada(String token, UsuarioVista usuario) {
}
