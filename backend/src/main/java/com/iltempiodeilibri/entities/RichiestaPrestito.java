package com.iltempiodeilibri.entities;

import com.iltempiodeilibri.dto.DurataPrestito;
import com.iltempiodeilibri.dto.StatoRichiesta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Un cliente chiede che un libro gli arrivi a casa.
 * <p>
 * Non è ancora un prestito: la copia resta a scaffale finché l'admin della sua sede
 * non approva. All'approvazione nasce il {@link Prestito} vero, e solo allora la copia esce.
 */
@Entity
@Table(name = "richieste_prestito")
@Getter
@Setter
@NoArgsConstructor
public class RichiestaPrestito {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "libro_id", nullable = false)
    private Libro libro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DurataPrestito durata = DurataPrestito.MEDIA;

    @Column(name = "indirizzo_consegna", nullable = false, columnDefinition = "TEXT")
    private String indirizzoConsegna;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private StatoRichiesta stato = StatoRichiesta.IN_ATTESA;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Chi ha deciso, quando, e perché se ha rifiutato
    @ManyToOne
    @JoinColumn(name = "admin_id")
    private User admin;

    @Column(name = "decisa_at")
    private Instant decisaAt;

    @Column(columnDefinition = "TEXT")
    private String motivo;

    // Il prestito nato dall'approvazione
    @OneToOne
    @JoinColumn(name = "prestito_id")
    private Prestito prestito;

    public RichiestaPrestito(User user, Libro libro, DurataPrestito durata, String indirizzoConsegna) {
        this.user = user;
        this.libro = libro;
        this.durata = durata;
        this.indirizzoConsegna = indirizzoConsegna;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
