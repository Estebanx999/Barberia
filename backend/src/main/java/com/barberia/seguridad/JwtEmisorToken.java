package com.barberia.seguridad;

import com.barberia.dominio.Usuario;
import com.barberia.servicio.autenticacion.EmisorToken;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

/** Emite tokens JWT firmados con HS256 que contienen el id, el rol y el nombre del usuario. */
@Component
public class JwtEmisorToken implements EmisorToken {

    static final String EMISOR = "barberia";

    private final JwtEncoder codificador;
    private final PropiedadesSeguridad propiedades;
    private final Clock reloj;

    public JwtEmisorToken(JwtEncoder codificador, PropiedadesSeguridad propiedades, Clock reloj) {
        this.codificador = codificador;
        this.propiedades = propiedades;
        this.reloj = reloj;
    }

    @Override
    public String emitir(Usuario usuario) {
        Instant ahora = Instant.now(reloj);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(EMISOR)
                .issuedAt(ahora)
                .expiresAt(ahora.plus(propiedades.duracionToken()))
                .subject(usuario.getId().toString())
                .claim(SeguridadConfig.CLAIM_ROL, usuario.getRol().name())
                .claim("nombre", usuario.getNombre())
                .build();
        JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).build();
        return codificador.encode(JwtEncoderParameters.from(cabecera, claims)).getTokenValue();
    }
}
