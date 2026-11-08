package com.barberia.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Barbero que atiende cortes. Su perfil (descripción y foto) es lo que ve el
 * cliente al elegir quién lo atiende. esDueno() es la pregunta que usa
 * AtencionFacade para elegir la política de comisión.
 */
@Entity
@DiscriminatorValue("BARBERO")
public class Barbero extends Usuario {

    @Column(length = 300)
    private String descripcion;

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    protected Barbero() {
        // requerido por JPA
    }

    public Barbero(String nombre, String correo, String telefono) {
        super(nombre, correo, telefono);
    }

    /** Datos que el cliente ve al elegir barbero. Los textos vacíos se guardan como nulos. */
    public void actualizarPerfil(String descripcion, String fotoUrl) {
        this.descripcion = textoOpcional(descripcion);
        this.fotoUrl = textoOpcional(fotoUrl);
    }

    private static String textoOpcional(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public boolean esDueno() {
        return false;
    }

    @Override
    public Rol getRol() {
        return Rol.BARBERO;
    }
}
