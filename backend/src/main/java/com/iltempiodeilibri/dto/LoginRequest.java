package com.iltempiodeilibri.dto;

import jakarta.validation.constraints.NotBlank;

// username accetta sia il nome utente sia l'email: sul sito esiste una sola pagina di accesso
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {
}
