package com.iltempiodeilibri.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SegnaLettoRequest(
        @NotNull UUID libroId,
        @NotNull Boolean letto
) {
}
