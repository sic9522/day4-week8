package com.iltempiodeilibri.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "prestiti")
@Getter
@Setter
@NoArgsConstructor
public class Prestito {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "libro_id", nullable = false)
    private Libro libro;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "data_riconsegna_prevista", nullable = false)
    private LocalDate dataRiconsegnaPrevista;

    @Column(name = "data_riconsegna_effettiva")
    private LocalDate dataRiconsegnaEffettiva;

    // Il cliente chiede di riportare il libro; il prestito si chiude solo quando un admin approva
    @Column(name = "richiesta_restituzione")
    private Instant richiestaRestituzione;

    // Se ha dichiarato di averlo letto: alla chiusura il segno passa nella sua libreria privata
    @Column(name = "letto_dichiarato")
    private Boolean lettoDichiarato;

    // Tariffa congelata all'apertura: se le costanti cambiano, i prestiti già aperti non si riscrivono
    @Column(name = "costo_noleggio", nullable = false, precision = 4, scale = 2)
    private BigDecimal costoNoleggio;

    @Column(name = "penale_riscossa", precision = 4, scale = 2)
    private BigDecimal penaleRiscossa;

    // Sconti decisi dall'admin alla riconsegna, separati: uno sul noleggio, uno sulla penale.
    // Tenerli distinti serve a sapere poi che cosa è stato scontato, non solo quanto.
    @Column(name = "sconto_noleggio")
    private Integer scontoNoleggio;

    @Column(name = "sconto_penale")
    private Integer scontoPenale;

    // Rettifiche a mano dell'admin (ritardo condonato, pagamento annullato): chi, quando e perché.
    // Sono soldi mossi senza il cliente davanti: senza traccia sarebbero invisibili.
    @Column(name = "rettifica_motivo", columnDefinition = "TEXT")
    private String rettificaMotivo;

    @ManyToOne
    @JoinColumn(name = "rettifica_admin_id")
    private User rettificaAdmin;

    @Column(name = "rettifica_at")
    private Instant rettificaAt;

    // Quanto è stato effettivamente incassato alla chiusura
    @Column(name = "totale_pagato", precision = 5, scale = 2)
    private BigDecimal totalePagato;

    // true se il prestito è stato prorogato
    @Column(name = "is_extended", nullable = false, columnDefinition = "boolean default false")
    private boolean extended = false;

    // Admin che ha aperto il prestito
    @ManyToOne(optional = false)
    @JoinColumn(name = "admin_open_id", nullable = false)
    private User adminOpen;

    // Admin che ha chiuso il prestito (NULL finché è aperto)
    @ManyToOne
    @JoinColumn(name = "admin_close_id")
    private User adminClose;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
