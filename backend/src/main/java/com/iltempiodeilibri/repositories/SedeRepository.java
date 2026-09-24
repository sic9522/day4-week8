package com.iltempiodeilibri.repositories;

import com.iltempiodeilibri.entities.Sede;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SedeRepository extends JpaRepository<Sede, UUID> {

    Optional<Sede> findByNomeIgnoreCase(String nome);

    List<Sede> findAll(Sort sort);
}
