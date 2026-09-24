package com.iltempiodeilibri.repositories;

import com.iltempiodeilibri.entities.Notifica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface NotificaRepository extends JpaRepository<Notifica, UUID> {

    Page<Notifica> findByDestinatarioId(UUID destinatarioId, Pageable pageable);

    long countByDestinatarioIdAndLettaFalse(UUID destinatarioId);

    @Modifying
    @Query("update Notifica n set n.letta = true where n.destinatario.id = :userId and n.letta = false")
    int segnaTutteLette(UUID userId);
}
