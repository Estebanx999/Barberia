package com.barberia.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/** Cuerpo de POST /api/auth/login (HU-22, HU-23, HU-24). */
public record InicioSesionRequest(
        @NotBlank String correo,
        @NotBlank @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String contrasena) {
}
