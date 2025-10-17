package br.com.bgrbarbosa.product_catalog.repository;

import br.com.bgrbarbosa.product_catalog.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
}
