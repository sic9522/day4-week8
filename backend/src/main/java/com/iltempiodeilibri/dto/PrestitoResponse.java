package com.iltempiodeilibri.dto;

import com.iltempiodeilibri.entities.Libro;
import com.iltempiodeilibri.entities.Prestito;
import com.iltempiodeilibri.entities.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PrestitoResponse(
        UUID id,
        UtenteBreve user,
        LibroBreve libro,
        Instant createdAt,
        LocalDate dataRiconsegnaPrevista,
        LocalDate dataRiconsegnaEffettiva,
        BigDecimal costoNoleggio,
        BigDecimal penaleRiscossa,
        // Penale maturata a oggi su un prestito ancora aperto e scaduto; null se chiuso o in tempo
        BigDecimal penaleMaturata,
        // Quanto costerebbe chiudere il prestito adesso: noleggio + penale maturata
        BigDecimal dovutoAOggi,
        Integer scontoNoleggio,
        Integer scontoPenale,
        BigDecimal totalePagato,
        Instant richiestaRestituzione,
        Boolean lettoDichiarato,
        String rettificaMotivo,
        UtenteBreve rettificaAdmin,
        Instant rettificaAt,
        boolean extended,
        StatoPrestito stato,
        UtenteBreve adminOpen,
        UtenteBreve adminClose
) {
    public record UtenteBreve(UUID id, String email, String nome, String cognome) {
        public static UtenteBreve di(User u) {
            return u == null ? null : new UtenteBreve(u.getId(), u.getEmail(), u.getNome(), u.getCognome());
        }
    }

    public record LibroBreve(UUID id, BigDecimal isbn, String titolo, String autore) {
        public static LibroBreve di(Libro l) {
            return new LibroBreve(l.getId(), l.getIsbn(), l.getTitolo(), l.getAutore());
        }
    }

    public static PrestitoResponse of(Prestito p, LocalDate oggi, BigDecimal penaleMaturata) {
        StatoPrestito stato = p.getDataRiconsegnaEffettiva() != null ? StatoPrestito.CHIUSO
                : p.getDataRiconsegnaPrevista().isBefore(oggi) ? StatoPrestito.IN_RITARDO
                : StatoPrestito.APERTO;

        BigDecimal dovuto = null;
        if (p.getDataRiconsegnaEffettiva() == null) {
            dovuto = p.getCostoNoleggio();
            if (penaleMaturata != null) {
                dovuto = dovuto.add(penaleMaturata);
            }
        }

        return new PrestitoResponse(p.getId(), UtenteBreve.di(p.getUser()), LibroBreve.di(p.getLibro()),
                p.getCreatedAt(), p.getDataRiconsegnaPrevista(), p.getDataRiconsegnaEffettiva(),
                p.getCostoNoleggio(), p.getPenaleRiscossa(), penaleMaturata, dovuto,
                p.getScontoNoleggio(), p.getScontoPenale(), p.getTotalePagato(),
                p.getRichiestaRestituzione(), p.getLettoDichiarato(),
                p.getRettificaMotivo(), UtenteBreve.di(p.getRettificaAdmin()), p.getRettificaAt(),
                p.isExtended(), stato,
                UtenteBreve.di(p.getAdminOpen()), UtenteBreve.di(p.getAdminClose()));
    }
}
