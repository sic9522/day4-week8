package com.iltempiodeilibri.repositories;

import com.iltempiodeilibri.entities.Prestito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface PrestitoRepository extends JpaRepository<Prestito, UUID>, JpaSpecificationExecutor<Prestito> {

    // Utente, libro e admin caricati con JOIN nella stessa query, niente N+1
    @Override
    @EntityGraph(attributePaths = {"user", "libro", "adminOpen", "adminClose"})
    Page<Prestito> findAll(Specification<Prestito> spec, Pageable pageable);

    // Tutti i prestiti di un utente: serve al riepilogo economico
    @EntityGraph(attributePaths = {"libro"})
    List<Prestito> findByUserId(UUID userId);

    // Restituzioni chieste dai clienti e ancora da approvare
    @EntityGraph(attributePaths = {"user", "libro", "adminOpen"})
    Page<Prestito> findByRichiestaRestituzioneNotNullAndDataRiconsegnaEffettivaIsNull(Pageable pageable);

    // Chi ha in mano le copie di un titolo, e da quando: prestiti aperti su quel libro
    @EntityGraph(attributePaths = {"user", "libro", "adminOpen"})
    List<Prestito> findByLibroIdAndDataRiconsegnaEffettivaIsNullOrderByDataRiconsegnaPrevistaAsc(UUID libroId);

}
