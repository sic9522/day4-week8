package com.iltempiodeilibri.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RispondiSegnalazioneRequest(
        @NotNull UUID id,
        @Size(max = 2000) String risposta,
        @NotNull Boolean risolta
) {
}
