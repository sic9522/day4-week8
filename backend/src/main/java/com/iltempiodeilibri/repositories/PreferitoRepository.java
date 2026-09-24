package com.iltempiodeilibri.repositories;

import com.iltempiodeilibri.entities.Preferito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PreferitoRepository extends JpaRepository<Preferito, UUID> {

    Page<Preferito> findByUserId(UUID userId, Pageable pageable);

    Page<Preferito> findByUserIdAndLetto(UUID userId, boolean letto, Pageable pageable);

    Optional<Preferito> findByUserIdAndLibroId(UUID userId, UUID libroId);

    boolean existsByUserIdAndLibroId(UUID userId, UUID libroId);
}
