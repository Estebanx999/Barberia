package com.barberia.dominio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    @Test
    void cadaTipoDeUsuarioDeclaraSuRol() {
        assertEquals(Rol.CLIENTE, new Cliente("Juan", null, null).getRol());
        assertEquals(Rol.BARBERO, new Barbero("Carlos", null, null).getRol());
        assertEquals(Rol.DUENO, new Dueno("Oscar", null, null).getRol());
    }

    @Test
    void normalizaElCorreo() {
        assertEquals("juan@correo.com", new Cliente("Juan", "  Juan@Correo.COM ", null).getCorreo());
        assertNull(new Cliente("Juan", "  ", null).getCorreo());
    }

    @Test
    void soloTieneCuentaSiTieneContrasena() {
        Cliente cliente = new Cliente("Juan", "juan@correo.com", null);
        assertFalse(cliente.tieneCuenta());
        cliente.asignarContrasenaHash("$2a$10$hash");
        assertTrue(cliente.tieneCuenta());
    }

    @Test
    void unUsuarioSinCorreoNoPuedeTenerCuenta() {
        Cliente sinCorreo = new Cliente("Pedro", null, null);
        assertThrows(IllegalStateException.class, () -> sinCorreo.asignarContrasenaHash("$2a$10$hash"));
    }
}
