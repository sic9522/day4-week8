package com.iltempiodeilibri.controllers;

import com.iltempiodeilibri.dto.DecisioneRichiestaRequest;
import com.iltempiodeilibri.dto.NuovaRichiestaRequest;
import com.iltempiodeilibri.dto.PageResponse;
import com.iltempiodeilibri.dto.RichiestaResponse;
import com.iltempiodeilibri.services.RichiestaPrestitoService;
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

// Richieste di consegna a domicilio: le apre il cliente, le decide l'admin della sua sede
@RestController
@RequestMapping("/api/richieste")
@RequiredArgsConstructor
public class RichiestaPrestitoController {

    private final RichiestaPrestitoService richiesteService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RichiestaResponse apri(@Valid @RequestBody NuovaRichiestaRequest request,
                                  @AuthenticationPrincipal Jwt jwt) {
        return richiesteService.apri(request, UUID.fromString(jwt.getSubject()));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/mie")
    public PageResponse<RichiestaResponse> mie(@PageableDefault(size = 20) Pageable pageable,
                                               @AuthenticationPrincipal Jwt jwt) {
        return richiesteService.mie(UUID.fromString(jwt.getSubject()), pageable);
    }

    @PreAuthorize("hasAnyRole('Admin', 'SuperUser')")
    @GetMapping("/daApprovare")
    public PageResponse<RichiestaResponse> daApprovare(@PageableDefault(size = 20) Pageable pageable,
                                                       @AuthenticationPrincipal Jwt jwt) {
        return richiesteService.daApprovare(UUID.fromString(jwt.getSubject()), pageable);
    }

    // Approvare apre il prestito: 409 se nel frattempo non ci sono più copie
    @PreAuthorize("hasAnyRole('Admin', 'SuperUser')")
    @PatchMapping("/decidi")
    public RichiestaResponse decidi(@Valid @RequestBody DecisioneRichiestaRequest request,
                                    @AuthenticationPrincipal Jwt jwt) {
        return richiesteService.decidi(request, UUID.fromString(jwt.getSubject()));
    }
}
