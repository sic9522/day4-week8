package com.iltempiodeilibri.dto;

import com.iltempiodeilibri.entities.Costante;

// Ogni fascia ha una durata in giorni e una tariffa, entrambe modificabili dal SuperUser
public enum DurataPrestito {
    BREVE(Costante.PRESTITO_DURATA_BREVE, Costante.PRESTITO_COSTO_BREVE),
    MEDIA(Costante.PRESTITO_DURATA_MEDIA, Costante.PRESTITO_COSTO_MEDIA),
    LUNGA(Costante.PRESTITO_DURATA_LUNGA, Costante.PRESTITO_COSTO_LUNGA);

    private final String chiaveDurata;
    private final String chiaveCosto;

    DurataPrestito(String chiaveDurata, String chiaveCosto) {
        this.chiaveDurata = chiaveDurata;
        this.chiaveCosto = chiaveCosto;
    }

    public String chiaveCostante() {
        return chiaveDurata;
    }

    public String chiaveCosto() {
        return chiaveCosto;
    }
}
