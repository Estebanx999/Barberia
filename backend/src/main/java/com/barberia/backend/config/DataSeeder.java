package com.barberia.backend.config;

import com.barberia.backend.domain.Barbero;
import com.barberia.backend.domain.Cliente;
import com.barberia.backend.domain.Dueno;
import com.barberia.backend.repository.BarberoRepository;
import com.barberia.backend.repository.ClienteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Carga datos demo para poder evidenciar el flujo principal del MVP en el
 * prototipo funcional (seccion 4 del Entregable 2) sin depender de un
 * registro manual previo.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final BarberoRepository barberoRepository;
    private final ClienteRepository clienteRepository;

    public DataSeeder(BarberoRepository barberoRepository, ClienteRepository clienteRepository) {
        this.barberoRepository = barberoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    public void run(String... args) {
        if (barberoRepository.count() > 0) {
            return;
        }

        Dueno dueno = new Dueno();
        dueno.setNombre("Esteban Cruz");
        dueno.setCorreo("dueno@barberia.com");
        dueno.setPasswordHash("1234");
        barberoRepository.save(dueno);

        Barbero carlos = new Barbero();
        carlos.setNombre("Carlos Ramirez");
        carlos.setCorreo("carlos@barberia.com");
        carlos.setPasswordHash("1234");
        carlos.setTipo(Barbero.Tipo.FIJO);
        barberoRepository.save(carlos);

        Barbero luis = new Barbero();
        luis.setNombre("Luis Pena");
        luis.setCorreo("luis@barberia.com");
        luis.setPasswordHash("1234");
        luis.setTipo(Barbero.Tipo.FIJO);
        barberoRepository.save(luis);

        Cliente andres = new Cliente();
        andres.setNombre("Andres Gomez");
        andres.setCorreo("andres.gomez@correo.com");
        andres.setPasswordHash("1234");
        clienteRepository.save(andres);
    }
}
