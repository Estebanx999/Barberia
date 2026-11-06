package com.barberia.web;

import com.barberia.servicio.atencion.DatosCliente;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Cuerpo de POST /api/cortes (HU-28). El barbero es el usuario autenticado.
 * Se envía clienteId si el cliente ya está registrado, o clienteNombre (y
 * clienteTelefono opcional) si es nuevo.
 */
public record CorteSinCitaRequest(
        Long clienteId,
        String clienteNombre,
        String clienteTelefono,
        @NotBlank String servicio,
        @NotNull @Positive BigDecimal valorCobrado) {

    @JsonIgnore
    @AssertTrue(message = "Indique clienteId o clienteNombre")
    public boolean isClienteIndicado() {
        return datosCliente().estaIndicado();
    }

    public DatosCliente datosCliente() {
        return new DatosCliente(clienteId, clienteNombre, clienteTelefono);
    }
}
