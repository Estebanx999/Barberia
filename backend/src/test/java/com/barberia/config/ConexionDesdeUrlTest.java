package com.barberia.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConexionDesdeUrlTest {

    private final ConexionDesdeUrl conexion = new ConexionDesdeUrl();

    @Test
    void traduceLaUrlDeLaNubeAPropiedadesJdbc() {
        Map<String, Object> propiedades = ConexionDesdeUrl.propiedades(
                "postgresql://barberia:cl%40ve+1@dpg-abc-a.virginia-postgres.render.com:6543/barberia?sslmode=require");

        assertEquals("jdbc:postgresql://dpg-abc-a.virginia-postgres.render.com:6543/barberia?sslmode=require",
                propiedades.get("spring.datasource.url"));
        assertEquals("barberia", propiedades.get("spring.datasource.username"));
        assertEquals("cl@ve+1", propiedades.get("spring.datasource.password"));
    }

    @Test
    void usaElPuertoDePostgresSiLaUrlNoLoTrae() {
        assertEquals("jdbc:postgresql://dpg-abc-a:5432/barberia",
                ConexionDesdeUrl.propiedades("postgres://u:p@dpg-abc-a/barberia").get("spring.datasource.url"));
    }

    @Test
    void rechazaUrlsQueNoSonDePostgres() {
        assertThrows(IllegalArgumentException.class, () -> ConexionDesdeUrl.propiedades("mysql://u:p@h/base"));
        assertThrows(IllegalArgumentException.class, () -> ConexionDesdeUrl.propiedades("postgresql://u:p@h"));
    }

    @Test
    void reemplazaLaConexionLocalCuandoHayDatabaseUrl() {
        MockEnvironment entorno = new MockEnvironment()
                .withProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/barberia")
                .withProperty("DATABASE_URL", "postgresql://render:secreto@dpg-abc-a/barberia");

        conexion.postProcessEnvironment(entorno, null);

        assertEquals("jdbc:postgresql://dpg-abc-a:5432/barberia", entorno.getProperty("spring.datasource.url"));
        assertEquals("render", entorno.getProperty("spring.datasource.username"));
    }

    @Test
    void dbUrlTienePrioridadSobreDatabaseUrl() {
        MockEnvironment entorno = new MockEnvironment()
                .withProperty("DB_URL", "jdbc:postgresql://db:5432/barberia")
                .withProperty("DATABASE_URL", "postgresql://render:secreto@dpg-abc-a/barberia");

        conexion.postProcessEnvironment(entorno, null);

        assertNull(entorno.getProperty("spring.datasource.username"));
    }
}
