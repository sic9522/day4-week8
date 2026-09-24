package com.iltempiodeilibri.services;

import com.iltempiodeilibri.dto.NuovaSegnalazioneRequest;
import com.iltempiodeilibri.dto.PageResponse;
import com.iltempiodeilibri.dto.RispondiSegnalazioneRequest;
import com.iltempiodeilibri.dto.SegnalazioneResponse;
import com.iltempiodeilibri.entities.Prestito;
import com.iltempiodeilibri.entities.Segnalazione;
import com.iltempiodeilibri.repositories.PrestitoRepository;
import com.iltempiodeilibri.repositories.SegnalazioneRepository;
import com.iltempiodeilibri.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SegnalazioneService {

    private static final Sort PIU_RECENTI = Sort.by(Sort.Direction.DESC, "createdAt");

    private final SegnalazioneRepository segnalazioneRepository;
    private final PrestitoRepository prestitoRepository;
    private final UserRepository userRepository;

    @Transactional
    public SegnalazioneResponse apri(NuovaSegnalazioneRequest r, UUID userId) {
        Prestito prestito = prestitoRepository.findById(r.idPrestito())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prestito non trovato"));

        // Si può segnalare solo un proprio prestito: l'utente arriva dal JWT, non dal corpo
        if (!prestito.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Questo prestito non è tuo");
        }

        return SegnalazioneResponse.of(
                segnalazioneRepository.save(new Segnalazione(prestito, r.testo().trim())));
    }

    @Transactional(readOnly = true)
    public PageResponse<SegnalazioneResponse> mie(UUID userId, Pageable pageable) {
        return PageResponse.of(segnalazioneRepository
                .findByPrestitoUserId(userId, ordina(pageable))
                .map(SegnalazioneResponse::of));
    }

    @Transactional(readOnly = true)
    public PageResponse<SegnalazioneResponse> tutte(Boolean risolta, Pageable pageable) {
        Pageable richiesta = ordina(pageable);
        return PageResponse.of((risolta == null
                ? segnalazioneRepository.findAll(richiesta)
                : segnalazioneRepository.findByRisolta(risolta, richiesta))
                .map(SegnalazioneResponse::of));
    }

    @Transactional
    public SegnalazioneResponse rispondi(RispondiSegnalazioneRequest r, UUID adminId) {
        Segnalazione segnalazione = segnalazioneRepository.findById(r.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Segnalazione non trovata"));
        segnalazione.setRisposta(r.risposta());
        segnalazione.setRisolta(r.risolta());
        segnalazione.setAdmin(userRepository.getReferenceById(adminId));
        return SegnalazioneResponse.of(segnalazione);
    }

    private static Pageable ordina(Pageable pageable) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), PIU_RECENTI);
    }
}
