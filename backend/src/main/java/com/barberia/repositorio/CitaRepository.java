package com.barberia.repositorio;

import com.barberia.dominio.Cita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    /** Citas de un barbero en un día: base de la validación de cruce (HU-07, RN-04) y de su calendario (HU-05). */
    List<Cita> findByBarberoIdAndFecha(Long barberoId, LocalDate fecha);

    /** Todas las citas de un día: calendario general del dueño (HU-06). */
    List<Cita> findByFechaOrderByHoraInicio(LocalDate fecha);
}
