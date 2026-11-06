package com.barberia.seguridad;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/** Configuración de los tokens (prefijo barberia.seguridad). El secreto llega por la variable JWT_SECRETO. */
@ConfigurationProperties(prefix = "barberia.seguridad")
public record PropiedadesSeguridad(String jwtSecreto, Duration duracionToken) {

    private static final int LONGITUD_MINIMA_SECRETO = 32;

    public PropiedadesSeguridad {
        if (jwtSecreto == null || jwtSecreto.getBytes(StandardCharsets.UTF_8).length < LONGITUD_MINIMA_SECRETO) {
            throw new IllegalArgumentException(
                    "barberia.seguridad.jwt-secreto (JWT_SECRETO) debe tener al menos 32 caracteres");
        }
        if (duracionToken == null || duracionToken.isZero() || duracionToken.isNegative()) {
            throw new IllegalArgumentException("barberia.seguridad.duracion-token debe ser positiva");
        }
    }
}
