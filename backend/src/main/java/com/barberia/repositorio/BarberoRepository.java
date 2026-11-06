package com.barberia.repositorio;

import com.barberia.dominio.Barbero;
import org.springframework.data.jpa.repository.JpaRepository;

/** Incluye al dueño, que también es un Barbero (puede atender cortes). */
public interface BarberoRepository extends JpaRepository<Barbero, Long> {
}
