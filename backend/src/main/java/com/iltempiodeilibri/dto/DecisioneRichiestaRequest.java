package com.iltempiodeilibri.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

// Approvare apre il prestito vero; rifiutare vuole un motivo da riferire al cliente
public record DecisioneRichiestaRequest(
        @NotNull UUID id,
        @NotNull Boolean approva,
        @Size(max = 500) String motivo
) {
}
