package com.barberia.repositorio;

import com.barberia.dominio.Corte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CorteRepository extends JpaRepository<Corte, Long> {

    /** Cortes del periodo (ambos extremos incluidos): fuente de los reportes de ingresos y utilidad. */
    List<Corte> findByFechaBetween(LocalDate desde, LocalDate hasta);

    boolean existsByCitaId(Long citaId);
}
