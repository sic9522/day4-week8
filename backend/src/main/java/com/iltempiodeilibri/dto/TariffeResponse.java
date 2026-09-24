package com.iltempiodeilibri.dto;

import java.math.BigDecimal;
import java.util.List;

// Il listino, leggibile da chiunque: un cliente deve poter sapere quanto costa prima di chiedere
public record TariffeResponse(
        List<Fascia> fasce,
        BigDecimal penaleGiornaliera,
        BigDecimal penaleMassima
) {
    public record Fascia(DurataPrestito durata, int giorni, BigDecimal costo) {
    }
}
