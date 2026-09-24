package com.iltempiodeilibri.services;

import com.iltempiodeilibri.dto.*;
import com.iltempiodeilibri.entities.Costante;
import com.iltempiodeilibri.entities.Prestito;
import com.iltempiodeilibri.repositories.LibroRepository;
import com.iltempiodeilibri.repositories.PrestitoRepository;
import com.iltempiodeilibri.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrestitoService {

    private static final ZoneId ZONA = ZoneId.systemDefault();

    // Colonne ordinabili: nome nel parametro sort -> proprietà JPA
    private static final Map<String, String> SORT_CONSENTITI = Map.of(
            "createdAt", "createdAt",
            "dataRiconsegnaPrevista", "dataRiconsegnaPrevista",
            "dataRiconsegnaEffettiva", "dataRiconsegnaEffettiva",
            "penaleRiscossa", "penaleRiscossa",
            "extended", "extended",
            "titolo", "libro.titolo",
            "email", "user.email",
            "cognome", "user.cognome",
            "nome", "user.nome");
    private static final Sort SORT_PREDEFINITO = Sort.by(Sort.Direction.DESC, "createdAt");

    private final PrestitoRepository prestitoRepository;
    private final LibroRepository libroRepository;
    private final UserRepository userRepository;
    private final CostanteService costanteService;
    private final PreferitoService preferitoService;

    @Transactional
    public PrestitoResponse apri(NuovoPrestitoRequest r, UUID adminId) {
        if (!userRepository.existsById(r.userId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato");
        }
        if (!libroRepository.existsById(r.libroId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Libro non trovato");
        }
        // Decremento atomico: con 0 copie disponibili nessuna riga viene aggiornata
        if (libroRepository.prendiCopia(r.libroId()) == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nessuna copia disponibile per questo libro");
        }

        DurataPrestito durata = r.durata() != null ? r.durata() : DurataPrestito.MEDIA;
        // Prestito su misura: i giorni indicati dall'admin battono la fascia
        int giorni = r.giorni() != null ? r.giorni() : costanteService.intero(durata.chiaveCostante());
        BigDecimal costo = r.costoNoleggio() != null
                ? r.costoNoleggio().setScale(2, RoundingMode.HALF_UP)
                : (r.giorni() != null ? tariffaPer(giorni) : costanteService.importo(durata.chiaveCosto()));

        Prestito prestito = new Prestito();
        prestito.setUser(userRepository.getReferenceById(r.userId()));
        prestito.setLibro(libroRepository.getReferenceById(r.libroId()));
        prestito.setAdminOpen(userRepository.getReferenceById(adminId));
        prestito.setDataRiconsegnaPrevista(LocalDate.now(ZONA).plusDays(giorni));
        // La tariffa si congela ora: se domani il SuperUser la cambia, questo prestito non cambia prezzo
        prestito.setCostoNoleggio(costo);
        prestitoRepository.save(prestito);

        return risposta(prestito);
    }

    @Transactional
    public PrestitoResponse chiudi(ChiudiPrestitoRequest r, UUID adminId) {
        Prestito prestito = trova(r.idPrestito());
        if (prestito.getDataRiconsegnaEffettiva() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Prestito già chiuso");
        }
        LocalDate oggi = LocalDate.now(ZONA);
        BigDecimal penale = calcolaPenale(prestito.getDataRiconsegnaPrevista(), oggi);

        prestito.setDataRiconsegnaEffettiva(oggi);
        prestito.setPenaleRiscossa(penale);
        prestito.setScontoNoleggio(r.scontoNoleggio());
        prestito.setScontoPenale(r.scontoPenale());
        // Noleggio e penale si scontano separatamente. Entrambi restano registrati al lordo,
        // così si vede sempre che cosa è stato concesso e su che cosa.
        prestito.setTotalePagato(
                applicaSconto(prestito.getCostoNoleggio(), r.scontoNoleggio())
                        .add(penale == null ? BigDecimal.ZERO : applicaSconto(penale, r.scontoPenale())));
        prestito.setAdminClose(userRepository.getReferenceById(adminId));
        libroRepository.restituisciCopia(prestito.getLibro().getId());

        // Il segno di lettura: quello scelto dall'admin allo sportello, altrimenti
        // quello che il cliente aveva dichiarato chiedendo la restituzione
        Boolean letto = r.letto() != null ? r.letto() : prestito.getLettoDichiarato();
        if (letto != null) {
            preferitoService.segnaDallaRiconsegna(
                    prestito.getUser().getId(), prestito.getLibro().getId(), letto);
        }
        return risposta(prestito);
    }

    // Il cliente chiede di riportare il libro. Il prestito NON si chiude qui:
    // resta aperto finché un admin non approva, così la copia non rientra in catalogo prima del tempo.
    @Transactional
    public PrestitoResponse richiediRestituzione(RichiestaRestituzioneRequest r, UUID userId) {
        Prestito prestito = trova(r.idPrestito());

        if (!prestito.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Questo prestito non è tuo");
        }
        if (prestito.getDataRiconsegnaEffettiva() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Prestito già chiuso");
        }
        if (prestito.getRichiestaRestituzione() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Restituzione già richiesta: attendi l'approvazione");
        }

        prestito.setRichiestaRestituzione(Instant.now());
        prestito.setLettoDichiarato(r.letto());
        return risposta(prestito);
    }

    // Durata su misura senza prezzo indicato: si paga la prima fascia che la copre,
    // l'ultima se la supera. Le fasce sono in ordine crescente di giorni.
    private BigDecimal tariffaPer(int giorni) {
        DurataPrestito scelta = DurataPrestito.LUNGA;
        for (DurataPrestito d : DurataPrestito.values()) {
            if (costanteService.intero(d.chiaveCostante()) >= giorni) {
                scelta = d;
                break;
            }
        }
        return costanteService.importo(scelta.chiaveCosto());
    }

    /**
     * Rettifiche a mano dell'admin: condonare un ritardo o annullare un pagamento.
     * Restano registrati chi ha rettificato, quando e perché: qui si muovono soldi
     * senza che il cliente sia presente, e una riga cambiata senza traccia non si spiega più.
     */
    @Transactional
    public PrestitoResponse rettifica(RettificaPrestitoRequest r, UUID adminId) {
        Prestito prestito = trova(r.idPrestito());
        LocalDate oggi = LocalDate.now(ZONA);

        switch (r.azione()) {
            case ANNULLA_RITARDO -> {
                if (prestito.getDataRiconsegnaEffettiva() == null) {
                    // Aperto: la scadenza slitta a oggi, la penale riparte da zero
                    if (prestito.getDataRiconsegnaPrevista().isBefore(oggi)) {
                        prestito.setDataRiconsegnaPrevista(oggi);
                    }
                } else {
                    // Chiuso: la penale sparisce e resta il solo noleggio, con il suo sconto
                    prestito.setPenaleRiscossa(null);
                    prestito.setTotalePagato(
                            applicaSconto(prestito.getCostoNoleggio(), prestito.getScontoNoleggio()));
                }
            }
            case ANNULLA_PAGAMENTO -> {
                if (prestito.getDataRiconsegnaEffettiva() == null) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Il prestito è ancora aperto: non c'è nessun pagamento da annullare");
                }
                prestito.setTotalePagato(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            }
        }

        prestito.setRettificaMotivo(r.motivo().trim());
        prestito.setRettificaAdmin(userRepository.getReferenceById(adminId));
        prestito.setRettificaAt(Instant.now());
        return risposta(prestito, oggi);
    }

    // Chi ha in mano le copie di un titolo e quando dovrebbero rientrare.
    // Le copie in prestito, non i numeri: un admin al banco ha bisogno del nome e della data.
    @Transactional(readOnly = true)
    public List<PrestitoResponse> copieFuori(UUID libroId) {
        LocalDate oggi = LocalDate.now(ZONA);
        return prestitoRepository
                .findByLibroIdAndDataRiconsegnaEffettivaIsNullOrderByDataRiconsegnaPrevistaAsc(libroId)
                .stream()
                .map(p -> risposta(p, oggi))
                .toList();
    }

    // Le richieste che aspettano un admin
    @Transactional(readOnly = true)
    public PageResponse<PrestitoResponse> daApprovare(Pageable pageable) {
        LocalDate oggi = LocalDate.now(ZONA);
        Pageable richiesta = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "richiestaRestituzione"));
        return PageResponse.of(prestitoRepository
                .findByRichiestaRestituzioneNotNullAndDataRiconsegnaEffettivaIsNull(richiesta)
                .map(p -> risposta(p, oggi)));
    }

    // Quadro economico di un cliente: incassato sui prestiti chiusi, maturato su quelli aperti e scaduti
    @Transactional(readOnly = true)
    public RiepilogoResponse riepilogo(UUID userId) {
        LocalDate oggi = LocalDate.now(ZONA);
        List<Prestito> prestiti = prestitoRepository.findByUserId(userId);

        BigDecimal pagato = BigDecimal.ZERO;
        BigDecimal daPagare = BigDecimal.ZERO;
        long aperti = 0;
        long inRitardo = 0;
        long chiusiInRitardo = 0;

        for (Prestito p : prestiti) {
            if (p.getDataRiconsegnaEffettiva() != null) {
                // Prestiti chiusi: quanto è stato davvero incassato, sconto già applicato
                if (p.getTotalePagato() != null) {
                    pagato = pagato.add(p.getTotalePagato());
                }
                if (p.getPenaleRiscossa() != null) {
                    chiusiInRitardo++;
                }
            } else {
                // Prestiti aperti: il noleggio è già dovuto, la penale matura giorno per giorno
                aperti++;
                daPagare = daPagare.add(p.getCostoNoleggio());
                BigDecimal maturata = calcolaPenale(p.getDataRiconsegnaPrevista(), oggi);
                if (maturata != null) {
                    inRitardo++;
                    daPagare = daPagare.add(maturata);
                }
            }
        }

        return new RiepilogoResponse(
                pagato.setScale(2, RoundingMode.HALF_UP),
                daPagare.setScale(2, RoundingMode.HALF_UP),
                prestiti.size(), aperti, inRitardo, chiusiInRitardo);
    }

    private BigDecimal applicaSconto(BigDecimal importo, Integer scontoPercentuale) {
        if (importo == null) {
            return null;
        }
        if (scontoPercentuale == null || scontoPercentuale <= 0) {
            return importo.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal resta = BigDecimal.valueOf(100L - Math.min(scontoPercentuale, 100));
        return importo.multiply(resta).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    @Transactional
    public PrestitoResponse estendi(EstendiPrestitoRequest r) {
        Prestito prestito = trova(r.idPrestito());
        if (prestito.getDataRiconsegnaEffettiva() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Prestito già chiuso");
        }
        if (prestito.isExtended()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Prestito già esteso: non è possibile estenderlo ancora");
        }
        prestito.setDataRiconsegnaPrevista(prestito.getDataRiconsegnaPrevista().plusDays(r.giorni()));
        prestito.setExtended(true);

        return risposta(prestito);
    }

    @Transactional(readOnly = true)
    public PageResponse<PrestitoResponse> cerca(PrestitoSearchParams params, UUID userForzato, Pageable pageable) {
        Sort sort = SearchUtils.traduciSort(pageable.getSort(), SORT_CONSENTITI, SORT_PREDEFINITO);
        Pageable richiesta = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        LocalDate oggi = LocalDate.now(ZONA);
        return PageResponse.of(prestitoRepository
                .findAll(PrestitoSpecifications.da(params, userForzato, oggi, ZONA), richiesta)
                .map(p -> risposta(p, oggi)));
    }

    // Giorni di ritardo x penale giornaliera, con tetto alla penale massima; NULL se riconsegnato in tempo
    private BigDecimal calcolaPenale(LocalDate prevista, LocalDate riconsegna) {
        long giorniRitardo = ChronoUnit.DAYS.between(prevista, riconsegna);
        if (giorniRitardo <= 0) {
            return null;
        }
        BigDecimal penale = costanteService.importo(Costante.PRESTITO_PENALE_GIORNALIERA)
                .multiply(BigDecimal.valueOf(giorniRitardo));
        return penale.min(costanteService.importo(Costante.PRESTITO_PENALE_MASSIMA))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private Prestito trova(UUID id) {
        return prestitoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prestito non trovato"));
    }

    private PrestitoResponse risposta(Prestito prestito) {
        return risposta(prestito, LocalDate.now(ZONA));
    }

    // La penale maturata si calcola solo sui prestiti ancora aperti: su uno chiuso vale quella riscossa
    private PrestitoResponse risposta(Prestito prestito, LocalDate oggi) {
        BigDecimal maturata = prestito.getDataRiconsegnaEffettiva() == null
                ? calcolaPenale(prestito.getDataRiconsegnaPrevista(), oggi)
                : null;
        return PrestitoResponse.of(prestito, oggi, maturata);
    }
}
