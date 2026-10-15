package com.barberia.backend.repository;

import com.barberia.backend.domain.Corte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CorteRepository extends JpaRepository<Corte, Long> {
    List<Corte> findByFechaHoraBetween(LocalDateTime desde, LocalDateTime hasta);
}
