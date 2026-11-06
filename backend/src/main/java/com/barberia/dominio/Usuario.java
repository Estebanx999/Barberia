package com.barberia.dominio;

import jakarta.persistence.*;

/**
 * Datos comunes de las personas del sistema (nivel 4: Usuario).
 * La autenticación y los roles quedan fuera del MVP: se asume resuelta.
 */
@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", length = 20)
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 160)
    private String correo;

    @Column(length = 30)
    private String telefono;

    protected Usuario() {
        // requerido por JPA
    }

    protected Usuario(String nombre, String correo, String telefono) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getTelefono() {
        return telefono;
    }
}
