package br.com.bgrbarbosa.product_catalog.repository;

import br.com.bgrbarbosa.product_catalog.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Role findByAuthority(String authority);
}
