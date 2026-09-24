package com.iltempiodeilibri.controllers;

import com.iltempiodeilibri.dto.NuovaSegnalazioneRequest;
import com.iltempiodeilibri.dto.PageResponse;
import com.iltempiodeilibri.dto.RispondiSegnalazioneRequest;
import com.iltempiodeilibri.dto.SegnalazioneResponse;
import com.iltempiodeilibri.services.SegnalazioneService;
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

@RestController
@RequestMapping("/api/segnalazioni")
@RequiredArgsConstructor
public class SegnalazioneController {

    private final SegnalazioneService segnalazioneService;

    // Il cliente segnala un problema su un proprio prestito
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SegnalazioneResponse apri(@Valid @RequestBody NuovaSegnalazioneRequest request,
                                     @AuthenticationPrincipal Jwt jwt) {
        return segnalazioneService.apri(request, UUID.fromString(jwt.getSubject()));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/mie")
    public PageResponse<SegnalazioneResponse> mie(@PageableDefault(size = 20) Pageable pageable,
                                                  @AuthenticationPrincipal Jwt jwt) {
        return segnalazioneService.mie(UUID.fromString(jwt.getSubject()), pageable);
    }

    @PreAuthorize("hasAnyRole('Admin', 'SuperUser')")
    @GetMapping("/all")
    public PageResponse<SegnalazioneResponse> all(@RequestParam(required = false) Boolean risolta,
                                                  @PageableDefault(size = 20) Pageable pageable) {
        return segnalazioneService.tutte(risolta, pageable);
    }

    @PreAuthorize("hasAnyRole('Admin', 'SuperUser')")
    @PatchMapping("/rispondi")
    public SegnalazioneResponse rispondi(@Valid @RequestBody RispondiSegnalazioneRequest request,
                                         @AuthenticationPrincipal Jwt jwt) {
        return segnalazioneService.rispondi(request, UUID.fromString(jwt.getSubject()));
    }
}
