package com.barberia.backend.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("CLIENTE")
@Getter
@Setter
@NoArgsConstructor
public class Cliente extends Usuario {

    private String telefono;

    @Override
    public String getRol() {
        return "CLIENTE";
    }
}
