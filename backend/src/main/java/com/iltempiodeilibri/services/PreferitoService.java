package com.iltempiodeilibri.services;

import com.iltempiodeilibri.dto.PageResponse;
import com.iltempiodeilibri.dto.PreferitoResponse;
import com.iltempiodeilibri.entities.Preferito;
import com.iltempiodeilibri.repositories.LibroRepository;
import com.iltempiodeilibri.repositories.PreferitoRepository;
import com.iltempiodeilibri.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PreferitoService {

    private static final Map<String, String> SORT_CONSENTITI = Map.of(
            "createdAt", "createdAt",
            "titolo", "libro.titolo",
            "autore", "libro.autore",
            "letto", "letto");
    private static final Sort SORT_PREDEFINITO = Sort.by(Sort.Direction.DESC, "createdAt");

    private final PreferitoRepository preferitoRepository;
    private final LibroRepository libroRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<PreferitoResponse> miei(UUID userId, Boolean letto, Pageable pageable) {
        Sort sort = SearchUtils.traduciSort(pageable.getSort(), SORT_CONSENTITI, SORT_PREDEFINITO);
        Pageable richiesta = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return PageResponse.of((letto == null
                ? preferitoRepository.findByUserId(userId, richiesta)
                : preferitoRepository.findByUserIdAndLetto(userId, letto, richiesta))
                .map(PreferitoResponse::of));
    }

    @Transactional
    public PreferitoResponse aggiungi(UUID userId, UUID libroId) {
        if (!libroRepository.existsById(libroId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Libro non trovato");
        }
        // Salvare due volte lo stesso libro non è un errore: si restituisce quello già presente
        Preferito preferito = preferitoRepository.findByUserIdAndLibroId(userId, libroId)
                .orElseGet(() -> preferitoRepository.save(new Preferito(
                        userRepository.getReferenceById(userId),
                        libroRepository.getReferenceById(libroId))));
        return PreferitoResponse.of(preferito);
    }

    @Transactional
    public void rimuovi(UUID userId, UUID libroId) {
        Preferito preferito = preferitoRepository.findByUserIdAndLibroId(userId, libroId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Questo libro non è tra i preferiti"));
        preferitoRepository.delete(preferito);
    }

    @Transactional
    public PreferitoResponse segnaLetto(UUID userId, UUID libroId, boolean letto) {
        Preferito preferito = preferitoRepository.findByUserIdAndLibroId(userId, libroId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Questo libro non è tra i preferiti"));
        preferito.setLetto(letto);
        return PreferitoResponse.of(preferito);
    }

    // Usata alla chiusura di un prestito: se il libro non è tra i preferiti lo aggiunge,
    // così il segno di lettura non va perso solo perché il cliente non lo aveva salvato
    @Transactional
    public void segnaDallaRiconsegna(UUID userId, UUID libroId, boolean letto) {
        Preferito preferito = preferitoRepository.findByUserIdAndLibroId(userId, libroId)
                .orElseGet(() -> preferitoRepository.save(new Preferito(
                        userRepository.getReferenceById(userId),
                        libroRepository.getReferenceById(libroId))));
        preferito.setLetto(letto);
    }
}
