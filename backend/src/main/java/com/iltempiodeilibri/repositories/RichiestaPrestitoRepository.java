package com.iltempiodeilibri.repositories;

import com.iltempiodeilibri.dto.StatoRichiesta;
import com.iltempiodeilibri.entities.RichiestaPrestito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RichiestaPrestitoRepository extends JpaRepository<RichiestaPrestito, UUID> {

    @EntityGraph(attributePaths = {"libro", "user", "admin"})
    Page<RichiestaPrestito> findByUserId(UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = {"libro", "user"})
    Page<RichiestaPrestito> findByStato(StatoRichiesta stato, Pageable pageable);

    // Un admin vede solo le richieste dei clienti della sua sede
    @EntityGraph(attributePaths = {"libro", "user"})
    Page<RichiestaPrestito> findByStatoAndUserSedeId(StatoRichiesta stato, UUID sedeId, Pageable pageable);

    boolean existsByUserIdAndLibroIdAndStato(UUID userId, UUID libroId, StatoRichiesta stato);
}
