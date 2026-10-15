package com.barberia.backend.repository;

import com.barberia.backend.domain.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface GastoRepository extends JpaRepository<Gasto, Long> {
    List<Gasto> findByFechaBetween(LocalDate desde, LocalDate hasta);
}
