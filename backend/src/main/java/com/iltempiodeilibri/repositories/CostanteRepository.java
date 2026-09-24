package com.iltempiodeilibri.repositories;

import com.iltempiodeilibri.entities.Costante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CostanteRepository extends JpaRepository<Costante, UUID> {

    Optional<Costante> findByChiave(String chiave);

}
