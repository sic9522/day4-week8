package com.iltempiodeilibri.controllers;

import com.iltempiodeilibri.dto.NotificaResponse;
import com.iltempiodeilibri.dto.PageResponse;
import com.iltempiodeilibri.services.NotificaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

// Ognuno legge solo le proprie: il destinatario arriva dal JWT, mai dalla richiesta
@RestController
@RequestMapping("/api/notifiche")
@RequiredArgsConstructor
public class NotificaController {

    private final NotificaService notificaService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/mie")
    public PageResponse<NotificaResponse> mie(@PageableDefault(size = 30) Pageable pageable,
                                              @AuthenticationPrincipal Jwt jwt) {
        return notificaService.mie(UUID.fromString(jwt.getSubject()), pageable);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/daLeggere")
    public Map<String, Long> daLeggere(@AuthenticationPrincipal Jwt jwt) {
        return Map.of("quante", notificaService.daLeggere(UUID.fromString(jwt.getSubject())));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/lette")
    public void lette(@AuthenticationPrincipal Jwt jwt) {
        notificaService.segnaTutteLette(UUID.fromString(jwt.getSubject()));
    }
}
