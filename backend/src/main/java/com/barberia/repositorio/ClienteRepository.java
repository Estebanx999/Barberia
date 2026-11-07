package com.barberia.repositorio;

import com.barberia.dominio.Cliente;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /** Busca solo entre clientes: un id de barbero no se acepta como cliente. */
    @Query("select c from Cliente c where c.id = :id")
    Optional<Cliente> buscarCliente(@Param("id") Long id);

    /**
     * Clientes cuyo nombre o teléfono contiene el texto (HU-28). El patrón debe
     * llegar en minúsculas y con los comodines % y _ escapados con '\'.
     */
    @Query("""
            select c from Cliente c
            where lower(c.nombre) like :patron escape '\\'
               or c.telefono like :patron escape '\\'
            order by c.nombre
            """)
    List<Cliente> buscarPorNombreOTelefono(@Param("patron") String patron, Pageable pagina);
}
