package com.iltempiodeilibri.dto;

// Ricerca libera su nome, cognome, email e username; ruolo filtra per nome del ruolo (User, Admin, SuperUser)
public record UserSearchParams(
        String q,
        String ruolo
) {
}
