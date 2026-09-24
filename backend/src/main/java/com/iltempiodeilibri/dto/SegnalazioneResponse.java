package com.iltempiodeilibri.dto;

import com.iltempiodeilibri.entities.Segnalazione;

import java.time.Instant;
import java.util.UUID;

public record SegnalazioneResponse(
        UUID id,
        UUID idPrestito,
        String titoloLibro,
        String cliente,
        String testo,
        String risposta,
        boolean risolta,
        Instant createdAt
) {
    public static SegnalazioneResponse of(Segnalazione s) {
        var u = s.getPrestito().getUser();
        return new SegnalazioneResponse(
                s.getId(),
                s.getPrestito().getId(),
                s.getPrestito().getLibro().getTitolo(),
                u.getNome() + " " + u.getCognome(),
                s.getTesto(),
                s.getRisposta(),
                s.isRisolta(),
                s.getCreatedAt());
    }
}
