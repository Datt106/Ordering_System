package com.orderingsystem.infrastructure.database;

import com.orderingsystem.core.domain.User;
import com.orderingsystem.core.domain.UserRole;

import java.util.List;
import java.util.Optional;

public class UserRepository extends BaseRepository {

    public void save(User user) {
        inTransaction(em -> {
            if (user.getId() == null) {
                em.persist(user);
            } else {
                em.merge(user);
            }
        });
    }

    public Optional<User> findByUsername(String username) {
        return query(em -> {
            List<User> results = em.createQuery(
                            "SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .setMaxResults(1)
                    .getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
        });
    }

    public Optional<User> findById(Long id) {
        return query(em -> Optional.ofNullable(em.find(User.class, id)));
    }

    public List<User> findByRole(UserRole role) {
        return query(em -> em.createQuery(
                        "SELECT u FROM User u WHERE u.role = :role ORDER BY u.username", User.class)
                .setParameter("role", role)
                .getResultList());
    }

    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }
}
