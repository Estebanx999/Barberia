package com.barberia.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del backend de Barberia.
 *
 * Entregable 2 - Diseno de Sistemas de Informacion.
 * Este backend materializa en codigo el diagrama de clases y las reglas de
 * negocio definidas en el Entregable 1, e incorpora tres patrones GoF
 * (Strategy, Builder y Observer) dentro del alcance del MVP (Release 1 del
 * Story Map): agendamiento de citas sin cruce de horario, registro de
 * cortes con calculo automatico de comision (60/40 barbero-dueno, 100% si
 * el corte lo realiza el dueno) y consulta de ingresos/gastos/utilidad neta.
 */
@SpringBootApplication
public class BarberiaBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BarberiaBackendApplication.class, args);
    }
}
