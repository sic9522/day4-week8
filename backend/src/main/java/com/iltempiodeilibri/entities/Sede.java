package com.iltempiodeilibri.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

// Una filiale della biblioteca. Ogni admin gestisce la sua, ogni cliente ne sceglie una all'iscrizione.
@Entity
@Table(name = "sedi")
@Getter
@Setter
@NoArgsConstructor
public class Sede {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String nome;

    @Column(nullable = false)
    private String via;

    @Column(nullable = false)
    private String citta;

    @Column(nullable = false, length = 5)
    private String cap;

    public Sede(String nome, String via, String citta, String cap) {
        this.nome = nome;
        this.via = via;
        this.citta = citta;
        this.cap = cap;
    }

    // "Biblioteca Acilia - Via d'Acilia 153, Roma, 00125"
    public String etichetta() {
        return nome + " - " + via + ", " + citta + ", " + cap;
    }
}
