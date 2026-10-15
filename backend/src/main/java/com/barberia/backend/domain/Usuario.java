package com.barberia.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Usuario (abstracta). Representa RN-06: toda persona que usa el sistema
 * (dueno, barbero o cliente) se autentica con credenciales propias.
 * Refinamiento de E1: se formaliza con JPA SINGLE_TABLE para permitir
 * login unico para los 3 roles (RF de autenticacion, HU-22/23/24).
 */
@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_usuario")
@Getter
@Setter
@NoArgsConstructor
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String correo;

    @com.fasterxml.jackson.annotation.JsonIgnore // nunca exponer la clave en las respuestas JSON
    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean activo = true;

    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public boolean iniciarSesion(String password) {
        // En una implementacion real se usa un PasswordEncoder (BCrypt).
        return this.activo && this.passwordHash != null && this.passwordHash.equals(password);
    }

    public abstract String getRol();
}
