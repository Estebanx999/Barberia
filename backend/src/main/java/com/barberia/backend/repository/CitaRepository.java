package com.barberia.backend.repository;

import com.barberia.backend.domain.Barbero;
import com.barberia.backend.domain.Cita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findByBarberoAndFecha(Barbero barbero, LocalDate fecha);
}
