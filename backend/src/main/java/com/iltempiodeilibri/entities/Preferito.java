package com.iltempiodeilibri.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

// La libreria privata di un cliente: i libri che ha salvato, con il segno di lettura
@Entity
@Table(name = "preferiti",
        uniqueConstraints = @UniqueConstraint(name = "preferiti_unique", columnNames = {"user_id", "libro_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Preferito {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "libro_id", nullable = false)
    private Libro libro;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean letto = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Preferito(User user, Libro libro) {
        this.user = user;
        this.libro = libro;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
