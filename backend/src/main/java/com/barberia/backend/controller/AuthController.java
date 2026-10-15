package com.barberia.backend.controller;

import com.barberia.backend.domain.Usuario;
import com.barberia.backend.dto.LoginRequest;
import com.barberia.backend.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** HU-22/HU-23/HU-24: Iniciar sesion (dueno/barbero/cliente) — un unico endpoint, rol segun el tipo de Usuario (RN-06). */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;

    public AuthController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(request.correo())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!usuario.iniciarSesion(request.password())) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales invalidas"));
        }

        return ResponseEntity.ok(Map.of(
                "id", usuario.getId(),
                "nombre", usuario.getNombre(),
                "rol", usuario.getRol()
        ));
    }
}
