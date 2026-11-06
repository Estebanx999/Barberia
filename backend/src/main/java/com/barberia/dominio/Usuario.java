package com.barberia.dominio;

import jakarta.persistence.*;

import java.util.Locale;

/**
 * Datos comunes de las personas del sistema (nivel 4: Usuario).
 * Cada subclase declara su rol, que define a qué funciones accede (HU-26).
 * La contraseña se guarda solo como hash; el dominio nunca ve el texto plano.
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

    @Column(length = 160, unique = true)
    private String correo;

    @Column(length = 30)
    private String telefono;

    /** Nulo para clientes registrados por un barbero (HU-28), que no tienen cuenta. */
    @Column(name = "contrasena_hash", length = 100)
    private String contrasenaHash;

    protected Usuario() {
        // requerido por JPA
    }

    protected Usuario(String nombre, String correo, String telefono) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        this.nombre = nombre.trim();
        this.correo = normalizarCorreo(correo);
        this.telefono = telefono;
    }

    /** Los correos se comparan sin distinguir mayúsculas ni espacios alrededor. */
    public static String normalizarCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            return null;
        }
        return correo.trim().toLowerCase(Locale.ROOT);
    }

    public abstract Rol getRol();

    public void asignarContrasenaHash(String hash) {
        if (hash == null || hash.isBlank()) {
            throw new IllegalArgumentException("El hash de la contraseña es obligatorio");
        }
        if (correo == null) {
            throw new IllegalStateException("Un usuario sin correo no puede tener cuenta");
        }
        this.contrasenaHash = hash;
    }

    public boolean tieneCuenta() {
        return contrasenaHash != null;
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

    public String getContrasenaHash() {
        return contrasenaHash;
    }
}
