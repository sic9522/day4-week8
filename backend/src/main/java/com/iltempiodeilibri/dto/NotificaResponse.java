package com.iltempiodeilibri.dto;

import com.iltempiodeilibri.entities.Notifica;

import java.time.Instant;
import java.util.UUID;

public record NotificaResponse(
        UUID id,
        String testo,
        String destinazione,
        boolean letta,
        Instant createdAt
) {
    public static NotificaResponse of(Notifica n) {
        return new NotificaResponse(n.getId(), n.getTesto(), n.getDestinazione(), n.isLetta(), n.getCreatedAt());
    }
}
