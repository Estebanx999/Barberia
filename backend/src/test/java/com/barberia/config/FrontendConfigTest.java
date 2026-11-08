package com.barberia.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FrontendConfigTest {

    private final FrontendConfig.RecursoDelFrontend recursos = new FrontendConfig.RecursoDelFrontend();
    private final Resource ubicacion = new ClassPathResource("frontend-prueba/");

    @Test
    void sirveLosArchivosQueExisten() throws IOException {
        assertEquals("app.js", recursos.getResource("assets/app.js", ubicacion).getFilename());
    }

    @Test
    void lasRutasDeLaAplicacionRespondenConIndex() throws IOException {
        assertEquals("index.html", recursos.getResource("cliente/agendar", ubicacion).getFilename());
        assertEquals("index.html", recursos.getResource("", ubicacion).getFilename());
    }

    @Test
    void noReemplazaLaApiNiArchivosQueFaltan() throws IOException {
        assertNull(recursos.getResource("api/no-existe", ubicacion));
        assertNull(recursos.getResource("assets/falta.js", ubicacion));
    }

    @Test
    void sinFrontendCompiladoNoSirveNada() throws IOException {
        assertNull(recursos.getResource("cliente", new ClassPathResource("no-hay-frontend/")));
    }
}
