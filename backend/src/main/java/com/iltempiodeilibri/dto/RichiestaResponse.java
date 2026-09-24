package com.iltempiodeilibri.dto;

import com.iltempiodeilibri.entities.RichiestaPrestito;

import java.time.Instant;
import java.util.UUID;

public record RichiestaResponse(
        UUID id,
        PrestitoResponse.UtenteBreve user,
        PrestitoResponse.LibroBreve libro,
        DurataPrestito durata,
        String indirizzoConsegna,
        StatoRichiesta stato,
        Instant createdAt,
        Instant decisaAt,
        String motivo,
        UUID idPrestito,
        String sede
) {
    public static RichiestaResponse of(RichiestaPrestito r) {
        return new RichiestaResponse(
                r.getId(),
                PrestitoResponse.UtenteBreve.di(r.getUser()),
                PrestitoResponse.LibroBreve.di(r.getLibro()),
                r.getDurata(),
                r.getIndirizzoConsegna(),
                r.getStato(),
                r.getCreatedAt(),
                r.getDecisaAt(),
                r.getMotivo(),
                r.getPrestito() == null ? null : r.getPrestito().getId(),
                r.getUser().getSede() == null ? null : r.getUser().getSede().getNome());
    }
}
