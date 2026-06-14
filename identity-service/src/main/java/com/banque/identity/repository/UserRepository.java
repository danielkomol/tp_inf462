package com.banque.identity.repository;

import com.banque.identity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * REPOSITORY UTILISATEURS
 * Spring génère automatiquement le SQL
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // SELECT COUNT(*) FROM users WHERE email = ?
    boolean existsByEmail(String email);

    // SELECT COUNT(*) FROM users WHERE telephone = ?
    boolean existsByTelephone(String telephone);
}
