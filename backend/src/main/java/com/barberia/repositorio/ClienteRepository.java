package com.barberia.repositorio;

import com.barberia.dominio.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /** Busca solo entre clientes: un id de barbero no se acepta como cliente. */
    @Query("select c from Cliente c where c.id = :id")
    Optional<Cliente> buscarCliente(@Param("id") Long id);
}
