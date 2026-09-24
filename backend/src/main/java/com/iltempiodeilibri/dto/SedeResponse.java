package com.iltempiodeilibri.dto;

import com.iltempiodeilibri.entities.Sede;

import java.util.UUID;

public record SedeResponse(
        UUID id,
        String nome,
        String via,
        String citta,
        String cap,
        String etichetta
) {
    public static SedeResponse of(Sede s) {
        return s == null ? null
                : new SedeResponse(s.getId(), s.getNome(), s.getVia(), s.getCitta(), s.getCap(), s.etichetta());
    }
}
