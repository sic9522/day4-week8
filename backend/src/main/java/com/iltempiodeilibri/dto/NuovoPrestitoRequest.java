package com.iltempiodeilibri.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Un prestito nasce su una fascia standard oppure su misura.
 * <p>
 * {@code giorni} sovrascrive la durata della fascia; {@code costoNoleggio} sovrascrive la tariffa.
 * Se si indicano i giorni ma non il prezzo, si paga la prima fascia che copre quella durata.
 */
public record NuovoPrestitoRequest(
        @NotNull UUID userId,
        @NotNull UUID libroId,
        DurataPrestito durata,
        @Positive @Max(365) Integer giorni,
        @DecimalMin("0.00") @DecimalMax("99.99") BigDecimal costoNoleggio
) {
}
