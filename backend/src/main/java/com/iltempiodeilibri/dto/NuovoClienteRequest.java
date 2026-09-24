package com.iltempiodeilibri.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

// Usata sia dall'iscrizione fatta da un admin sia dalla registrazione pubblica:
// in entrambi i casi il cliente sceglie la sede a cui si appoggia
public record NuovoClienteRequest(
        @NotBlank @Email String email,
        @Size(min = 3, max = 40) String username,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotNull @Past LocalDate dataDiNascita,
        @NotBlank String nome,
        @NotBlank String cognome,
        @NotBlank String indirizzo,
        @NotNull UUID sedeId
) {
}
