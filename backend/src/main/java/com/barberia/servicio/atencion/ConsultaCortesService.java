package com.barberia.servicio.atencion;

import com.barberia.dominio.Corte;
import com.barberia.dominio.Usuario;
import com.barberia.reporte.Periodo;
import com.barberia.repositorio.ClienteRepository;
import com.barberia.repositorio.CorteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * HU-23 / HU-26: cortes y comisiones de un barbero en un periodo. Solo lee;
 * la fachada de atención queda limitada a registrar (HU-12, 27 y 28).
 */
@Service
@Transactional(readOnly = true)
public class ConsultaCortesService {

    private final CorteRepository cortes;
    private final ClienteRepository clientes;

    public ConsultaCortesService(CorteRepository cortes, ClienteRepository clientes) {
        this.cortes = cortes;
        this.clientes = clientes;
    }

    public ResumenCortes cortesDelBarbero(Long barberoId, Periodo periodo) {
        List<Corte> lista = cortes.findByBarberoIdAndFechaBetweenOrderByFechaDescIdDesc(
                barberoId, periodo.desde(), periodo.hasta());

        Set<Long> idsClientes = lista.stream().map(Corte::getClienteId).collect(Collectors.toSet());
        Map<Long, String> nombres = clientes.findAllById(idsClientes).stream()
                .collect(Collectors.toMap(Usuario::getId, Usuario::getNombre));

        List<CorteVista> vistas = lista.stream()
                .map(corte -> CorteVista.de(corte, nombres.get(corte.getClienteId())))
                .toList();
        BigDecimal totalCobrado = vistas.stream()
                .map(CorteVista::valorCobrado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalComision = vistas.stream()
                .map(vista -> vista.comision().montoBarbero())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ResumenCortes(periodo, vistas.size(), totalCobrado, totalComision, vistas);
    }

    public record ResumenCortes(
            Periodo periodo,
            int cantidadCortes,
            BigDecimal totalCobrado,
            BigDecimal totalComision,
            List<CorteVista> cortes) {
    }
}
