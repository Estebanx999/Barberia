package com.barberia.servicio.cliente;

import com.barberia.dominio.Cliente;
import com.barberia.repositorio.ClienteRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * HU-28: el barbero busca a un cliente que llegó sin cita para no registrarlo
 * dos veces. Solo devuelve nombre y teléfono, lo necesario para reconocerlo.
 */
@Service
@Transactional(readOnly = true)
public class BusquedaClientesService {

    static final int LONGITUD_MINIMA = 2;
    static final int MAXIMO_RESULTADOS = 10;

    private final ClienteRepository clientes;

    public BusquedaClientesService(ClienteRepository clientes) {
        this.clientes = clientes;
    }

    public List<ClienteResumen> buscar(String texto) {
        String limpio = texto == null ? "" : texto.trim();
        if (limpio.length() < LONGITUD_MINIMA) {
            return List.of();
        }
        String patron = "%" + escaparComodines(limpio.toLowerCase(Locale.ROOT)) + "%";
        return clientes.buscarPorNombreOTelefono(patron, PageRequest.of(0, MAXIMO_RESULTADOS)).stream()
                .map(ClienteResumen::de)
                .toList();
    }

    /** Evita que un % o _ escrito por el usuario funcione como comodín. */
    static String escaparComodines(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    public record ClienteResumen(Long id, String nombre, String telefono) {

        static ClienteResumen de(Cliente cliente) {
            return new ClienteResumen(cliente.getId(), cliente.getNombre(), cliente.getTelefono());
        }
    }
}
