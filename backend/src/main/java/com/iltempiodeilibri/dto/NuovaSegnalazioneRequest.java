package com.iltempiodeilibri.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record NuovaSegnalazioneRequest(
        @NotNull UUID idPrestito,
        @NotBlank @Size(max = 2000) String testo
) {
}
