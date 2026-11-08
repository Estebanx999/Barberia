package com.barberia.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Sirve la aplicación web compilada (frontend) desde el mismo backend cuando la
 * imagen la incluye en classpath:/static/. Así la nube publica un solo servicio:
 * al abrir la página se despierta el backend que atiende la API.
 * En desarrollo y con Docker Compose el frontend lo sirve Vite o Nginx y esta
 * configuración no encuentra archivos que servir.
 */
@Configuration
public class FrontendConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registro) {
        registro.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new RecursoDelFrontend());
    }

    /**
     * Devuelve el archivo pedido si existe. Las rutas de la aplicación
     * (por ejemplo /cliente/agendar) no son archivos: responden con index.html y
     * React Router muestra la pantalla. Lo que empieza por api/ nunca se reemplaza.
     */
    static class RecursoDelFrontend extends PathResourceResolver {

        @Override
        protected Resource getResource(String ruta, Resource ubicacion) throws IOException {
            Resource archivo = ubicacion.createRelative(ruta);
            if (archivo.exists() && archivo.isReadable()) {
                return archivo;
            }
            if (ruta.startsWith("api/") || ruta.contains(".")) {
                return null;
            }
            Resource inicio = ubicacion.createRelative("index.html");
            return inicio.exists() && inicio.isReadable() ? inicio : null;
        }
    }
}
