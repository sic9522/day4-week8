package com.iltempiodeilibri.repositories;

import com.iltempiodeilibri.entities.TokenJwt;
import com.iltempiodeilibri.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TokenJwtRepository extends JpaRepository<TokenJwt, UUID> {

    Optional<TokenJwt> findByToken(String token);

    List<TokenJwt> findByUserAndRevocatoFalse(User user);

}
