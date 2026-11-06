package com.barberia.repositorio;

import com.barberia.dominio.Corte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CorteRepository extends JpaRepository<Corte, Long> {

    /** Cortes del periodo (ambos extremos incluidos). */
    List<Corte> findByFechaBetween(LocalDate desde, LocalDate hasta);

    boolean existsByCitaId(Long citaId);

    /**
     * Cantidad de cortes, ingresos brutos y comisiones pagadas a los barberos
     * en el periodo (ambos extremos incluidos). Fuente de HU-15 y HU-16.
     */
    @Query("""
            select new com.barberia.repositorio.TotalesCortes(
                count(c), sum(c.valorCobrado), sum(m.montoBarbero))
            from Corte c left join c.comision m
            where c.fecha between :desde and :hasta
            """)
    TotalesCortes totalizar(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
}
