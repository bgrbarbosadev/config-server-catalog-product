package br.com.bgrbarbosa.product_catalog.repository;

import br.com.bgrbarbosa.product_catalog.model.Category;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID>{
    boolean existsByNameCategory(String name);
}
