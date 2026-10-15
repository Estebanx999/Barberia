package com.barberia.backend.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Comision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double montoBarbero;
    private double montoDueno;
    private double porcentajeBarbero;
    private double porcentajeDueno;

    public Comision(double montoBarbero, double montoDueno, double porcentajeBarbero, double porcentajeDueno) {
        this.montoBarbero = montoBarbero;
        this.montoDueno = montoDueno;
        this.porcentajeBarbero = porcentajeBarbero;
        this.porcentajeDueno = porcentajeDueno;
    }
}
