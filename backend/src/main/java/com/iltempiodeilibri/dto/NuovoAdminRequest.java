package com.iltempiodeilibri.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

// Nominando un admin si indica anche la sede che gestirà: se non esiste viene creata
public record NuovoAdminRequest(
        @NotBlank @Email String email,
        @Size(min = 3, max = 40) String username,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotNull @Past LocalDate dataDiNascita,
        @NotBlank String nome,
        @NotBlank String cognome,
        @NotBlank String indirizzo,
        @NotBlank String nomeNegozio,
        @NotBlank String via,
        @NotBlank String citta,
        @NotBlank @Pattern(regexp = "\\d{5}", message = "Il CAP deve avere 5 cifre") String cap
) {
}
