package com.barberia.seguridad;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * HU-26: control de acceso por rol. La API no guarda sesión: cada solicitud
 * trae un token JWT (Authorization: Bearer) con el id y el rol del usuario.
 * Las reglas de "solo lo propio" (un barbero solo ve sus citas) se revisan
 * además en los controladores con UsuarioActual.
 */
@Configuration
public class SeguridadConfig {

    static final String CLAIM_ROL = "rol";

    @Bean
    public SecurityFilterChain filtroDeSeguridad(HttpSecurity http) throws Exception {
        http
                // Sin cookies de sesión no hay riesgo de CSRF: el token se envía en una cabecera.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(acceso -> acceso
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/registro", "/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/**").authenticated()
                        // Agenda
                        .requestMatchers(HttpMethod.GET, "/api/barberos", "/api/barberos/*/disponibilidad", "/api/servicios")
                        .authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/citas").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.GET, "/api/citas/mias").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.GET, "/api/citas").hasRole("DUENO")
                        .requestMatchers(HttpMethod.GET, "/api/barberos/*/citas").hasAnyRole("BARBERO", "DUENO")
                        // Atención (el dueño también corta)
                        .requestMatchers(HttpMethod.PATCH, "/api/citas/*/atendida").hasAnyRole("BARBERO", "DUENO")
                        .requestMatchers(HttpMethod.POST, "/api/citas/*/corte", "/api/cortes").hasAnyRole("BARBERO", "DUENO")
                        .requestMatchers(HttpMethod.GET, "/api/cortes/mios").hasAnyRole("BARBERO", "DUENO")
                        .requestMatchers(HttpMethod.GET, "/api/clientes").hasAnyRole("BARBERO", "DUENO")
                        // Módulo contable
                        .requestMatchers("/api/gastos/**", "/api/reportes/**").hasRole("DUENO")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(servidor -> servidor
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(convertidorDeRoles())));
        return http.build();
    }

    @Bean
    public PasswordEncoder codificadorDeContrasenas() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtEncoder jwtEncoder(PropiedadesSeguridad propiedades) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(clave(propiedades)));
    }

    @Bean
    public JwtDecoder jwtDecoder(PropiedadesSeguridad propiedades) {
        return NimbusJwtDecoder.withSecretKey(clave(propiedades)).macAlgorithm(MacAlgorithm.HS256).build();
    }

    /** Convierte el claim "rol" del token en la autoridad ROLE_x que usan las reglas de arriba. */
    private JwtAuthenticationConverter convertidorDeRoles() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName(CLAIM_ROL);
        roles.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter convertidor = new JwtAuthenticationConverter();
        convertidor.setJwtGrantedAuthoritiesConverter(roles);
        return convertidor;
    }

    private static SecretKey clave(PropiedadesSeguridad propiedades) {
        return new SecretKeySpec(propiedades.jwtSecreto().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
