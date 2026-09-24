package com.iltempiodeilibri.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Un avviso per una persona precisa.
 * <p>
 * Il testo si scrive quando il fatto accade e non si ricalcola più: se domani il libro
 * cambia titolo o il prestito si chiude, la notifica deve continuare a raccontare
 * quello che era successo allora.
 */
@Entity
@Table(name = "notifiche")
@Getter
@Setter
@NoArgsConstructor
public class Notifica {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private User destinatario;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String testo;

    // Dove porta l'avviso nel frontend, es. /prestiti oppure /console/prestiti
    @Column(nullable = false)
    private String destinazione;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean letta = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Notifica(User destinatario, String testo, String destinazione) {
        this.destinatario = destinatario;
        this.testo = testo;
        this.destinazione = destinazione;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
