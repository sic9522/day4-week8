package com.iltempiodeilibri.controllers;

import com.iltempiodeilibri.dto.NuovoPreferitoRequest;
import com.iltempiodeilibri.dto.PageResponse;
import com.iltempiodeilibri.dto.PreferitoResponse;
import com.iltempiodeilibri.dto.SegnaLettoRequest;
import com.iltempiodeilibri.services.PreferitoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// La libreria privata del cliente. Ogni operazione vale solo sui propri preferiti:
// l'utente viene dal JWT, mai dal corpo della richiesta
@RestController
@RequestMapping("/api/preferiti")
@RequiredArgsConstructor
public class PreferitoController {

    private final PreferitoService preferitoService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/miei")
    public PageResponse<PreferitoResponse> miei(@RequestParam(required = false) Boolean letto,
                                                @PageableDefault(size = 20) Pageable pageable,
                                                @AuthenticationPrincipal Jwt jwt) {
        return preferitoService.miei(UUID.fromString(jwt.getSubject()), letto, pageable);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PreferitoResponse aggiungi(@Valid @RequestBody NuovoPreferitoRequest request,
                                      @AuthenticationPrincipal Jwt jwt) {
        return preferitoService.aggiungi(UUID.fromString(jwt.getSubject()), request.libroId());
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{libroId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rimuovi(@PathVariable UUID libroId, @AuthenticationPrincipal Jwt jwt) {
        preferitoService.rimuovi(UUID.fromString(jwt.getSubject()), libroId);
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/letto")
    public PreferitoResponse segnaLetto(@Valid @RequestBody SegnaLettoRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        return preferitoService.segnaLetto(UUID.fromString(jwt.getSubject()), request.libroId(), request.letto());
    }
}
