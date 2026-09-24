package com.iltempiodeilibri.dto;

import com.iltempiodeilibri.entities.Preferito;

import java.time.Instant;
import java.util.UUID;

public record PreferitoResponse(
        UUID id,
        LibroResponse libro,
        boolean letto,
        Instant createdAt
) {
    public static PreferitoResponse of(Preferito p) {
        return new PreferitoResponse(p.getId(), LibroResponse.of(p.getLibro()), p.isLetto(), p.getCreatedAt());
    }
}
