package com.iltempiodeilibri.repositories;

import com.iltempiodeilibri.entities.Segnalazione;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SegnalazioneRepository extends JpaRepository<Segnalazione, UUID> {

    @EntityGraph(attributePaths = {"prestito", "prestito.libro", "prestito.user"})
    Page<Segnalazione> findByPrestitoUserId(UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = {"prestito", "prestito.libro", "prestito.user"})
    Page<Segnalazione> findByRisolta(boolean risolta, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"prestito", "prestito.libro", "prestito.user"})
    Page<Segnalazione> findAll(Pageable pageable);
}
