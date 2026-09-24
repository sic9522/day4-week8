package com.iltempiodeilibri.dto;

public enum AzioneRettifica {
    // Il ritardo non conta: su un prestito aperto sposta la scadenza a oggi,
    // su uno chiuso azzera la penale e ricalcola il totale
    ANNULLA_RITARDO,
    // Il cliente non deve niente per questo prestito: totale a zero
    ANNULLA_PAGAMENTO
}
