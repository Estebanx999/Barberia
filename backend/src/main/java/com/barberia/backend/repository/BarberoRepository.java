package com.barberia.backend.repository;

import com.barberia.backend.domain.Barbero;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BarberoRepository extends JpaRepository<Barbero, Long> {
    List<Barbero> findByActivoTrue();
}
