package com.iltempiodeilibri.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

// Il cliente chiede di riportare il libro e dichiara se lo ha letto.
// Il prestito resta aperto: si chiude solo quando un admin approva.
public record RichiestaRestituzioneRequest(
        @NotNull UUID idPrestito,
        Boolean letto
) {
}
