package com.iltempiodeilibri.dto;

import com.iltempiodeilibri.entities.User;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String username,
        String nome,
        String cognome,
        LocalDate dataDiNascita,
        String indirizzo,
        SedeResponse sede,
        List<String> ruoli,
        Instant createdAt
) {
    public static UserResponse of(User u, List<String> ruoli) {
        return new UserResponse(u.getId(), u.getEmail(), u.getUsername(), u.getNome(), u.getCognome(),
                u.getDataDiNascita(), u.getIndirizzo(), SedeResponse.of(u.getSede()), ruoli, u.getCreatedAt());
    }
}
