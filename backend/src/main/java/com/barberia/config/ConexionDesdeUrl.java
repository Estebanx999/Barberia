package com.barberia.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Permite configurar la base con una sola variable DATABASE_URL en el formato
 * que entregan los servicios en la nube (postgresql://usuario:clave@host:puerto/base).
 * La traduce a las propiedades spring.datasource que espera el driver JDBC.
 * Si se define DB_URL, esa tiene prioridad y DATABASE_URL se ignora.
 */
public class ConexionDesdeUrl implements EnvironmentPostProcessor {

    static final String VARIABLE = "DATABASE_URL";
    private static final int PUERTO_POR_DEFECTO = 5432;

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment entorno, SpringApplication aplicacion) {
        String url = entorno.getProperty(VARIABLE);
        if (url == null || url.isBlank() || entorno.containsProperty("DB_URL")) {
            return;
        }
        entorno.getPropertySources().addFirst(new MapPropertySource("conexionDesdeUrl", propiedades(url)));
    }

    static Map<String, Object> propiedades(String url) {
        URI uri = URI.create(url.trim());
        if (!"postgres".equals(uri.getScheme()) && !"postgresql".equals(uri.getScheme())) {
            throw new IllegalArgumentException(VARIABLE + " debe empezar por postgresql://");
        }
        if (uri.getHost() == null || uri.getPath() == null || uri.getPath().length() <= 1) {
            throw new IllegalArgumentException(VARIABLE + " debe indicar el servidor y el nombre de la base");
        }
        int puerto = uri.getPort() == -1 ? PUERTO_POR_DEFECTO : uri.getPort();
        String consulta = uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery();

        Map<String, Object> propiedades = new LinkedHashMap<>();
        propiedades.put("spring.datasource.url",
                "jdbc:postgresql://" + uri.getHost() + ":" + puerto + uri.getRawPath() + consulta);
        String credenciales = uri.getRawUserInfo();
        if (credenciales != null) {
            String[] partes = credenciales.split(":", 2);
            propiedades.put("spring.datasource.username", decodificar(partes[0]));
            if (partes.length == 2) {
                propiedades.put("spring.datasource.password", decodificar(partes[1]));
            }
        }
        return propiedades;
    }

    /** Decodifica %XX sin convertir '+' en espacio, que sí puede aparecer en una contraseña. */
    private static String decodificar(String texto) {
        return URLDecoder.decode(texto.replace("+", "%2B"), StandardCharsets.UTF_8);
    }
}
