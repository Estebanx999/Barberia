package com.barberia.repositorio;

import com.barberia.dominio.Barbero;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/** Incluye al dueño, que también es un Barbero (puede atender cortes). */
public interface BarberoRepository extends JpaRepository<Barbero, Long> {

    List<Barbero> findAllByOrderByNombre();

    /**
     * Bloquea la fila del barbero hasta el fin de la transacción. Serializa los
     * agendamientos concurrentes de un mismo barbero para que la validación de
     * cruce (RN-04) no se salte con dos solicitudes simultáneas.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Barbero b where b.id = :id")
    Optional<Barbero> findByIdParaAgendar(@Param("id") Long id);
}
