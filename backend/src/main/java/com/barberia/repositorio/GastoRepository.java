package com.barberia.repositorio;

import com.barberia.dominio.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface GastoRepository extends JpaRepository<Gasto, Long> {

    /** Gastos del periodo (ambos extremos incluidos). */
    List<Gasto> findByFechaBetween(LocalDate desde, LocalDate hasta);
}
