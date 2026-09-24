package com.iltempiodeilibri.dto;

import java.math.BigDecimal;

// Il quadro economico di un cliente. Gli importi li calcola sempre il server:
// totaleDaPagare è la penale maturata finora sui prestiti ancora aperti e già scaduti.
public record RiepilogoResponse(
        BigDecimal totalePagato,
        BigDecimal totaleDaPagare,
        long prestitiTotali,
        long prestitiAperti,
        long prestitiInRitardo,
        long prestitiChiusiInRitardo
) {
}
