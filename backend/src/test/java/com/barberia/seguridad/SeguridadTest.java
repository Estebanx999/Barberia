package com.barberia.seguridad;

import com.barberia.dominio.Dueno;
import com.barberia.dominio.Rol;
import com.barberia.servicio.AccesoDenegadoException;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalTime;

import static com.barberia.soporte.DatosDePrueba.*;
import static org.junit.jupiter.api.Assertions.*;

class SeguridadTest {

    private static final String SECRETO = "un-secreto-de-pruebas-con-mas-de-32-caracteres";

    private final SeguridadConfig config = new SeguridadConfig();
    private final PropiedadesSeguridad propiedades = new PropiedadesSeguridad(SECRETO, Duration.ofHours(8));

    @Test
    void elTokenLlevaElIdYElRolYSeValidaConElMismoSecreto() {
        var emisor = new JwtEmisorToken(config.jwtEncoder(propiedades), propiedades, relojEn(DIA, LocalTime.NOON));
        Dueno oscar = conId(new Dueno("Oscar", "oscar@barberia.com", null), 3);

        String token = emisor.emitir(oscar);
        // El decodificador valida la vigencia con la hora real; el token se emite
        // con reloj fijo, así que aquí solo se verifica la firma y el contenido.
        Jwt jwt = sinValidarVigencia().decode(token);

        assertEquals("3", jwt.getSubject());
        assertEquals("DUENO", jwt.getClaimAsString("rol"));
        assertEquals("Oscar", jwt.getClaimAsString("nombre"));
        assertEquals(jwt.getIssuedAt().plus(Duration.ofHours(8)), jwt.getExpiresAt());
    }

    @Test
    void unTokenFirmadoConOtroSecretoSeRechaza() {
        var otras = new PropiedadesSeguridad("otro-secreto-distinto-con-mas-de-32-caracteres", Duration.ofHours(8));
        var emisor = new JwtEmisorToken(config.jwtEncoder(otras), otras, Clock.systemUTC());
        String token = emisor.emitir(conId(new Dueno("Oscar", "oscar@barberia.com", null), 3));

        JwtDecoder decodificador = config.jwtDecoder(propiedades);
        assertThrows(JwtException.class, () -> decodificador.decode(token));
    }

    @Test
    void unTokenVigenteSeAcepta() {
        var emisor = new JwtEmisorToken(config.jwtEncoder(propiedades), propiedades, Clock.systemUTC());
        String token = emisor.emitir(conId(new Dueno("Oscar", "oscar@barberia.com", null), 3));

        assertEquals("3", config.jwtDecoder(propiedades).decode(token).getSubject());
    }

    @Test
    void elSecretoDebeTenerAlMenos32Caracteres() {
        assertThrows(IllegalArgumentException.class, () -> new PropiedadesSeguridad("corto", Duration.ofHours(8)));
        assertThrows(IllegalArgumentException.class, () -> new PropiedadesSeguridad(null, Duration.ofHours(8)));
        assertThrows(IllegalArgumentException.class, () -> new PropiedadesSeguridad(SECRETO, Duration.ZERO));
    }

    // --- HU-26 ---

    @Test
    void unBarberoSoloAccedeASuPropiaInformacion() {
        UsuarioActual carlos = new UsuarioActual(2L, Rol.BARBERO);
        assertDoesNotThrow(() -> carlos.verificarAccesoABarbero(2L));
        assertThrows(AccesoDenegadoException.class, () -> carlos.verificarAccesoABarbero(5L));
    }

    @Test
    void elDuenoAccedeALaInformacionDeTodos() {
        UsuarioActual oscar = new UsuarioActual(3L, Rol.DUENO);
        assertDoesNotThrow(() -> oscar.verificarAccesoABarbero(2L));
    }

    @Test
    void unClienteNoAccedeALaInformacionDeUnBarbero() {
        assertThrows(AccesoDenegadoException.class,
                () -> new UsuarioActual(7L, Rol.CLIENTE).verificarAccesoABarbero(2L));
    }

    private JwtDecoder sinValidarVigencia() {
        var decodificador = (NimbusJwtDecoder) config.jwtDecoder(propiedades);
        decodificador.setJwtValidator(jwt -> OAuth2TokenValidatorResult.success());
        return decodificador;
    }
}
