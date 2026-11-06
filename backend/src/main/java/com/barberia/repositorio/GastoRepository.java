package com.barberia.repositorio;

import com.barberia.dominio.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface GastoRepository extends JpaRepository<Gasto, Long> {

    /** Gastos del periodo (ambos extremos incluidos). */
    List<Gasto> findByFechaBetween(LocalDate desde, LocalDate hasta);

    /** Total y cantidad de gastos por concepto en el periodo (HU-29). */
    @Query("""
            select new com.barberia.repositorio.TotalPorConcepto(g.concepto, sum(g.valor), count(g))
            from Gasto g
            where g.fecha between :desde and :hasta
            group by g.concepto
            order by g.concepto
            """)
    List<TotalPorConcepto> totalizarPorConcepto(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    /** Suma de los gastos del periodo; nula si no hay gastos (HU-16). */
    @Query("select sum(g.valor) from Gasto g where g.fecha between :desde and :hasta")
    BigDecimal sumarValor(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
}
