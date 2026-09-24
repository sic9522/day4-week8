package com.iltempiodeilibri.services;

import com.iltempiodeilibri.dto.UserSearchParams;
import com.iltempiodeilibri.entities.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

final class UserSpecifications {

    private UserSpecifications() {
    }

    static Specification<User> da(UserSearchParams params) {
        return (root, query, cb) -> {
            List<Predicate> condizioni = new ArrayList<>();

            if (params != null && SearchUtils.presente(params.q())) {
                String testo = params.q();
                condizioni.add(cb.or(
                        SearchUtils.like(cb, root.get("nome"), testo),
                        SearchUtils.like(cb, root.get("cognome"), testo),
                        SearchUtils.like(cb, root.get("email"), testo),
                        SearchUtils.like(cb, root.get("username"), testo)));
            }

            if (params != null && SearchUtils.presente(params.ruolo())) {
                Join<Object, Object> ruoli = root.join("ruoli", JoinType.INNER);
                condizioni.add(cb.equal(ruoli.get("ruolo").get("ruolo"), params.ruolo().trim()));
                if (query != null) {
                    query.distinct(true);   // un utente con piu' ruoli comparirebbe piu' volte
                }
            }

            if (params != null && params.sedeId() != null) {
                condizioni.add(cb.equal(root.get("sede").get("id"), params.sedeId()));
            }

            return condizioni.isEmpty() ? cb.conjunction() : cb.and(condizioni.toArray(new Predicate[0]));
        };
    }
}
