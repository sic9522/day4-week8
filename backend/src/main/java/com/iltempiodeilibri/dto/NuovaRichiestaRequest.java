package com.iltempiodeilibri.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

// Il cliente chiede che un libro gli arrivi a casa.
// Se non indica un indirizzo si usa quello della sua anagrafica.
public record NuovaRichiestaRequest(
        @NotNull UUID libroId,
        DurataPrestito durata,
        @Size(max = 300) String indirizzoConsegna
) {
}
