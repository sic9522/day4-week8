package com.iltempiodeilibri.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record NuovoPreferitoRequest(
        @NotNull UUID libroId
) {
}
