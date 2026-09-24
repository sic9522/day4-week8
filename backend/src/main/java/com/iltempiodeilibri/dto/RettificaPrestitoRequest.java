package com.iltempiodeilibri.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

// Il motivo è obbligatorio: una rettifica sposta soldi senza che il cliente ne sappia nulla,
// e fra sei mesi nessuno ricorderà perché
public record RettificaPrestitoRequest(
        @NotNull UUID idPrestito,
        @NotNull AzioneRettifica azione,
        @NotBlank @Size(min = 5, max = 500) String motivo
) {
}
