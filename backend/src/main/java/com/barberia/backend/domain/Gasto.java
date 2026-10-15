package com.barberia.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** RN-03: los gastos operativos se restan de la utilidad bruta (40%) para obtener la utilidad neta. */
@Entity
@Table(name = "gastos")
@Getter
@Setter
@NoArgsConstructor
public class Gasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Dueno registradoPor;

    @Column(nullable = false)
    private double monto;

    @Column(nullable = false)
    private String categoria;

    private String descripcion;

    @Column(nullable = false)
    private LocalDate fecha = LocalDate.now();
}
