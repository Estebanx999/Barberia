package com.barberia.repositorio;

import com.barberia.dominio.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** El correo debe llegar normalizado (Usuario.normalizarCorreo). */
    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);
}
