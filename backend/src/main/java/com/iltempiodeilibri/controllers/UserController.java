package com.iltempiodeilibri.controllers;

import com.iltempiodeilibri.dto.LoginRequest;
import com.iltempiodeilibri.dto.LoginResponse;
import com.iltempiodeilibri.dto.NuovoAdminRequest;
import com.iltempiodeilibri.dto.NuovoClienteRequest;
import com.iltempiodeilibri.dto.PageResponse;
import com.iltempiodeilibri.dto.UserResponse;
import com.iltempiodeilibri.dto.UserSearchParams;
import com.iltempiodeilibri.services.UserService;
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
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Unica via d'ingresso al sito: si accede con nome utente oppure email
    @PreAuthorize("permitAll()")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }

    // Registrazione aperta, ma solo per i clienti: chi si iscrive sceglie la sede di appoggio
    @PreAuthorize("permitAll()")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody NuovoClienteRequest request) {
        return userService.creaCliente(request);
    }

    // Stessa cosa, ma fatta allo sportello da un admin
    @PreAuthorize("hasAnyRole('Admin', 'SuperUser')")
    @PostMapping("/newCliente")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse newCliente(@Valid @RequestBody NuovoClienteRequest request) {
        return userService.creaCliente(request);
    }

    // Gli Admin li nomina solo il SuperUser, indicando anche la sede che gestiranno
    @PreAuthorize("hasRole('SuperUser')")
    @PostMapping("/newAdmin")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse newAdmin(@Valid @RequestBody NuovoAdminRequest request) {
        return userService.creaAdmin(request);
    }

    // Elenco utenti con ricerca libera e filtro per ruolo: alimenta le sezioni Clienti e Admin
    @PreAuthorize("hasAnyRole('Admin', 'SuperUser')")
    @GetMapping("/all")
    public PageResponse<UserResponse> all(@ModelAttribute UserSearchParams params,
                                          @PageableDefault(size = 20) Pageable pageable) {
        return userService.cerca(params, pageable);
    }

    @PreAuthorize("hasAnyRole('Admin', 'SuperUser')")
    @GetMapping("/{id}")
    public UserResponse dettaglio(@PathVariable UUID id) {
        return userService.dettaglio(id);
    }

    // Invalida il JWT usato nella richiesta
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public void logout(@AuthenticationPrincipal Jwt jwt) {
        userService.logout(jwt.getTokenValue());
    }

    // Revoca il JWT attuale e ne restituisce uno nuovo con scadenza rinnovata
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/refresh")
    public LoginResponse refresh(@AuthenticationPrincipal Jwt jwt) {
        return userService.refresh(UUID.fromString(jwt.getSubject()), jwt.getTokenValue());
    }

    // Dati dell'utente del JWT, senza password
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return userService.me(UUID.fromString(jwt.getSubject()));
    }
}
