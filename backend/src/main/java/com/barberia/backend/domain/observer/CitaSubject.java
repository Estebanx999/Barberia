package com.barberia.backend.domain.observer;

/** Patron GoF: OBSERVER. Interfaz del "sujeto" observado (Cita). */
public interface CitaSubject {
    void agregarObservador(ObservadorCita observador);
    void notificarObservadores();
}
