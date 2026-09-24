package com.iltempiodeilibri.services;

import com.iltempiodeilibri.dto.*;
import com.iltempiodeilibri.entities.Libro;
import com.iltempiodeilibri.entities.RichiestaPrestito;
import com.iltempiodeilibri.entities.User;
import com.iltempiodeilibri.repositories.LibroRepository;
import com.iltempiodeilibri.repositories.PrestitoRepository;
import com.iltempiodeilibri.repositories.RichiestaPrestitoRepository;
import com.iltempiodeilibri.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

/**
 * Le richieste di consegna a domicilio.
 * <p>
 * Una richiesta non muove niente: la copia resta a scaffale finché un admin non approva.
 * Approvarla apre il prestito vero, con l'admin che approva registrato come chi lo ha aperto —
 * così resta chiaro chi ha dato via la copia.
 */
@Service
@RequiredArgsConstructor
public class RichiestaPrestitoService {

    private static final Sort PIU_RECENTI = Sort.by(Sort.Direction.DESC, "createdAt");

    private final RichiestaPrestitoRepository richiesteRepository;
    private final LibroRepository libroRepository;
    private final UserRepository userRepository;
    private final PrestitoRepository prestitoRepository;
    private final PrestitoService prestitoService;
    private final NotificaService notificaService;

    @Transactional
    public RichiestaResponse apri(NuovaRichiestaRequest r, UUID userId) {
        Libro libro = libroRepository.findById(r.libroId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Libro non trovato"));

        if (richiesteRepository.existsByUserIdAndLibroIdAndStato(userId, libro.getId(), StatoRichiesta.IN_ATTESA)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Hai già una richiesta aperta per questo libro");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utente non trovato"));

        // Senza un indirizzo indicato si spedisce dove il cliente abita
        String dove = r.indirizzoConsegna() == null || r.indirizzoConsegna().isBlank()
                ? user.getIndirizzo()
                : r.indirizzoConsegna().trim();

        RichiestaPrestito richiesta = richiesteRepository.save(new RichiestaPrestito(
                user, libro, r.durata() != null ? r.durata() : DurataPrestito.MEDIA, dove));

        // Avviso doppio: al cliente che la richiesta è partita, agli admin della sua sede che c'è da decidere
        String chi = user.getNome() + " " + user.getCognome();
        notificaService.avvisa(user,
                "La tua richiesta per «" + libro.getTitolo() + "» è stata inviata. Aspetta l'approvazione.",
                "/prestiti");
        notificaService.avvisaAdmin(user.getSede(),
                chi + " chiede «" + libro.getTitolo() + "» a domicilio.",
                "/console/prestiti");

        return RichiestaResponse.of(richiesta);
    }

    @Transactional(readOnly = true)
    public PageResponse<RichiestaResponse> mie(UUID userId, Pageable pageable) {
        return PageResponse.of(richiesteRepository
                .findByUserId(userId, ordina(pageable))
                .map(RichiestaResponse::of));
    }

    /** Un admin vede le richieste della propria sede; il SuperUser, che non ne ha una, le vede tutte. */
    @Transactional(readOnly = true)
    public PageResponse<RichiestaResponse> daApprovare(UUID adminId, Pageable pageable) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utente non trovato"));
        Pageable richiesta = ordina(pageable);

        var pagina = admin.getSede() == null
                ? richiesteRepository.findByStato(StatoRichiesta.IN_ATTESA, richiesta)
                : richiesteRepository.findByStatoAndUserSedeId(StatoRichiesta.IN_ATTESA, admin.getSede().getId(), richiesta);

        return PageResponse.of(pagina.map(RichiestaResponse::of));
    }

    @Transactional
    public RichiestaResponse decidi(DecisioneRichiestaRequest d, UUID adminId) {
        RichiestaPrestito richiesta = richiesteRepository.findById(d.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Richiesta non trovata"));

        if (richiesta.getStato() != StatoRichiesta.IN_ATTESA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Questa richiesta è già stata decisa");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utente non trovato"));

        // Un admin decide solo per i clienti della propria sede
        if (admin.getSede() != null) {
            var sedeCliente = richiesta.getUser().getSede();
            if (sedeCliente == null || !sedeCliente.getId().equals(admin.getSede().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Questo cliente non è della tua sede");
            }
        }

        if (Boolean.TRUE.equals(d.approva())) {
            // Solo ora la copia esce: se nel frattempo sono finite, l'apertura fallisce con 409
            PrestitoResponse prestito = prestitoService.apri(
                    new NuovoPrestitoRequest(richiesta.getUser().getId(), richiesta.getLibro().getId(),
                            richiesta.getDurata(), null, null),
                    adminId);
            richiesta.setPrestito(prestitoRepository.getReferenceById(prestito.id()));
            richiesta.setStato(StatoRichiesta.APPROVATA);
        } else {
            richiesta.setStato(StatoRichiesta.RIFIUTATA);
            richiesta.setMotivo(d.motivo() == null ? null : d.motivo().trim());
        }

        richiesta.setAdmin(admin);
        richiesta.setDecisaAt(Instant.now());

        // Avviso doppio anche qui: al cliente l'esito, all'admin la conferma di quel che ha fatto
        String titolo = richiesta.getLibro().getTitolo();
        boolean approvata = richiesta.getStato() == StatoRichiesta.APPROVATA;

        notificaService.avvisa(richiesta.getUser(),
                approvata
                        ? "«" + titolo + "» è in arrivo a " + richiesta.getIndirizzoConsegna() + "."
                        : "La richiesta per «" + titolo + "» è stata rifiutata"
                          + (richiesta.getMotivo() == null ? "." : ": " + richiesta.getMotivo()),
                "/prestiti");

        notificaService.avvisa(admin,
                (approvata ? "Hai approvato" : "Hai rifiutato") + " «" + titolo + "» per "
                        + richiesta.getUser().getNome() + " " + richiesta.getUser().getCognome() + ".",
                "/console/prestiti");

        return RichiestaResponse.of(richiesta);
    }

    private static Pageable ordina(Pageable pageable) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), PIU_RECENTI);
    }
}
