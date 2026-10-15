package com.barberia.backend.domain;

import com.barberia.backend.domain.comision.CalculadoraComisionStrategy;
import com.barberia.backend.domain.comision.ComisionBarberoEmpleadoStrategy;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("BARBERO")
@Getter
@Setter
@NoArgsConstructor
public class Barbero extends Usuario {

    public enum Tipo { FIJO, TEMPORAL }

    @Enumerated(EnumType.STRING)
    private Tipo tipo = Tipo.FIJO;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @JsonIgnore // evita recursion infinita Barbero -> Cita -> Barbero al serializar
    @OneToMany(mappedBy = "barbero")
    private List<Cita> citas = new ArrayList<>();

    @JsonIgnore
    @Transient
    private CalculadoraComisionStrategy calculadoraComision = new ComisionBarberoEmpleadoStrategy();

    @Override
    public String getRol() {
        return "BARBERO";
    }

    /**
     * Patron STRATEGY: delega el calculo en la estrategia asignada a este
     * barbero. Dueno sobrescribe la estrategia por defecto (ver Dueno).
     */
    public Comision calcularComision(double valorCobrado) {
        return calculadoraComision.calcular(valorCobrado);
    }

    /**
     * RN-04: un mismo horario no puede asignarse dos veces al mismo
     * barbero. RN-05: un barbero temporal solo puede agendarse en
     * temporada alta (validacion adicional a nivel de servicio).
     */
    public boolean tieneDisponibilidad(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        return citas.stream()
                .filter(c -> c.getFecha().equals(fecha) && c.getEstado() != Cita.Estado.CANCELADA)
                .noneMatch(c -> c.seSolapaCon(horaInicio, horaFin));
    }
}
