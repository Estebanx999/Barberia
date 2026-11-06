package com.barberia.web;

import com.barberia.seguridad.UsuarioActual;
import com.barberia.servicio.autenticacion.AutenticacionService;
import com.barberia.servicio.autenticacion.SesionIniciada;
import com.barberia.servicio.autenticacion.UsuarioVista;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AutenticacionController {

    private final AutenticacionService autenticacion;

    public AutenticacionController(AutenticacionService autenticacion) {
        this.autenticacion = autenticacion;
    }

    /** HU-21: registro de cliente. */
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioVista registrar(@Valid @RequestBody RegistroClienteRequest registro) {
        return autenticacion.registrarCliente(registro.nombre(), registro.correo(),
                registro.telefono(), registro.contrasena());
    }

    /** HU-22, HU-23, HU-24: inicio de sesión de dueño, barbero o cliente. */
    @PostMapping("/login")
    public SesionIniciada iniciarSesion(@Valid @RequestBody InicioSesionRequest credenciales) {
        return autenticacion.iniciarSesion(credenciales.correo(), credenciales.contrasena());
    }

    /** Datos del usuario autenticado (para restaurar la sesión en el frontend). */
    @GetMapping("/yo")
    public UsuarioVista yo(UsuarioActual usuario) {
        return autenticacion.obtener(usuario.id());
    }
}
