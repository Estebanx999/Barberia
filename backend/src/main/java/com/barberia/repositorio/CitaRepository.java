package com.barberia.repositorio;

import com.barberia.dominio.Cita;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    /** Citas de un barbero en un día: base de la validación de cruce (HU-07, RN-04). */
    List<Cita> findByBarberoIdAndFecha(Long barberoId, LocalDate fecha);

    /** Calendario de un barbero ordenado por hora (HU-05). */
    List<Cita> findByBarberoIdAndFechaOrderByHoraInicio(Long barberoId, LocalDate fecha);

    /** Todas las citas de un día con su barbero: calendario general del dueño (HU-06). */
    @EntityGraph(attributePaths = "barbero")
    List<Cita> findByFechaOrderByHoraInicio(LocalDate fecha);
}
