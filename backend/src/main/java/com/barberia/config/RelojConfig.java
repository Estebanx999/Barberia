package com.barberia.config;

import com.barberia.servicio.agenda.HorarioAtencion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/** Reloj inyectable: la hora actual se obtiene de aquí para poder fijarla en las pruebas. */
@Configuration
public class RelojConfig {

    @Bean
    public Clock reloj(HorarioAtencion horario) {
        return Clock.system(horario.zonaHoraria());
    }
}
