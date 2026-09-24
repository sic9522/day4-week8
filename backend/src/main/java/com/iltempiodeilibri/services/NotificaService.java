package com.iltempiodeilibri.services;

import com.iltempiodeilibri.dto.NotificaResponse;
import com.iltempiodeilibri.dto.PageResponse;
import com.iltempiodeilibri.entities.Notifica;
import com.iltempiodeilibri.entities.Sede;
import com.iltempiodeilibri.entities.User;
import com.iltempiodeilibri.repositories.NotificaRepository;
import com.iltempiodeilibri.repositories.RuoloUtenteRepository;
import com.iltempiodeilibri.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificaService {

    private static final Sort PIU_RECENTI = Sort.by(Sort.Direction.DESC, "createdAt");

    private final NotificaRepository notificaRepository;
    private final UserRepository userRepository;
    private final RuoloUtenteRepository ruoloUtenteRepository;

    @Transactional
    public void avvisa(User destinatario, String testo, String destinazione) {
        if (destinatario == null) {
            return;
        }
        notificaRepository.save(new Notifica(destinatario, testo, destinazione));
    }

    /**
     * Avvisa gli admin di una sede. Se la sede è nulla si avvisano tutti gli admin:
     * meglio un avviso di troppo che una richiesta che non raggiunge nessuno.
     */
    @Transactional
    public void avvisaAdmin(Sede sede, String testo, String destinazione) {
        List<User> admin = ruoloUtenteRepository.findAll().stream()
                .filter(ru -> "Admin".equals(ru.getRuolo().getRuolo()))
                .map(ru -> ru.getUser())
                .filter(u -> sede == null || (u.getSede() != null && u.getSede().getId().equals(sede.getId())))
                .toList();

        admin.forEach(a -> avvisa(a, testo, destinazione));
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificaResponse> mie(UUID userId, Pageable pageable) {
        Pageable richiesta = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), PIU_RECENTI);
        return PageResponse.of(notificaRepository.findByDestinatarioId(userId, richiesta).map(NotificaResponse::of));
    }

    @Transactional(readOnly = true)
    public long daLeggere(UUID userId) {
        return notificaRepository.countByDestinatarioIdAndLettaFalse(userId);
    }

    @Transactional
    public void segnaTutteLette(UUID userId) {
        notificaRepository.segnaTutteLette(userId);
    }

    @Transactional(readOnly = true)
    public User utente(UUID id) {
        return userRepository.findById(id).orElse(null);
    }
}
