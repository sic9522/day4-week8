package com.iltempiodeilibri.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

// Un problema segnalato dal cliente su un suo prestito: pagine mancanti, copia rovinata, ritardo non suo
@Entity
@Table(name = "segnalazioni")
@Getter
@Setter
@NoArgsConstructor
public class Segnalazione {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "prestito_id", nullable = false)
    private Prestito prestito;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String testo;

    @Column(columnDefinition = "TEXT")
    private String risposta;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean risolta = false;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private User admin;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Segnalazione(Prestito prestito, String testo) {
        this.prestito = prestito;
        this.testo = testo;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
