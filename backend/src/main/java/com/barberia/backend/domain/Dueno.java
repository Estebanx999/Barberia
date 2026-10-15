package com.barberia.backend.domain;

import com.barberia.backend.domain.comision.ComisionDuenoStrategy;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dueno extiende Barbero (el dueno tambien puede atender clientes),
 * conservando la decision de modelado de E1 confirmada por el usuario:
 * "el dueno es a su vez un barbero".
 */
@Entity
@DiscriminatorValue("DUENO")
@Getter
@Setter
@NoArgsConstructor
public class Dueno extends Barbero {

    public Dueno() {
        super();
        // Al construirse un Dueno se reemplaza la estrategia por defecto
        // (60/40) por la estrategia de dueno (100/0). Esta es la
        // sobrescritura polimorfica que en E1 solo era una nota del
        // diagrama; en E2 queda materializada via Strategy.
        setCalculadoraComision(new ComisionDuenoStrategy());
    }

    @Override
    public String getRol() {
        return "DUENO";
    }
}
