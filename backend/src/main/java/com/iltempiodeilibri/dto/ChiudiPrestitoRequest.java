package com.iltempiodeilibri.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

// Alla riconsegna l'admin può scontare separatamente il noleggio e la penale di ritardo.
// letto: se il cliente non lo ha già dichiarato chiedendo la restituzione.
public record ChiudiPrestitoRequest(
        @NotNull UUID idPrestito,
        @Min(0) @Max(100) Integer scontoNoleggio,
        @Min(0) @Max(100) Integer scontoPenale,
        Boolean letto
) {
}
