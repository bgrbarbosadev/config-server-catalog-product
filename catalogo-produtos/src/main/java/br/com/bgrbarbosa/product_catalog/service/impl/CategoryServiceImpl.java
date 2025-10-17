package br.com.bgrbarbosa.product_catalog.service.impl;

import br.com.bgrbarbosa.product_catalog.config.Messages;
import br.com.bgrbarbosa.product_catalog.model.Category;
import br.com.bgrbarbosa.product_catalog.repository.CategoryRepository;
import br.com.bgrbarbosa.product_catalog.service.CategoryService;
import br.com.bgrbarbosa.product_catalog.service.exception.IllegalArgumentException;
import br.com.bgrbarbosa.product_catalog.service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;

    @Override
    public Category insert(Category category) {
        if (repository.existsByNameCategory(category.getNameCategory())) {
            throw new IllegalArgumentException(Messages.ILLEGAL_ARGUMENT_EXCEPTION);
        }
        return repository.save(category);
    }

    @Override
    public List<Category> findAll(Pageable page) {
        return repository.findAll();
    }

    @Override
    public List<Category> findAll() {
        return repository.findAll();
    }

    @Override
    public Category findById(UUID uuid) {
        return repository.findById(uuid).orElseThrow(
                () -> new ResourceNotFoundException(Messages.RESOURCE_NOT_FOUND)
        );
    }

    @Override
    public void delete(UUID uuid) {
        if (!repository.existsById(uuid)) {
            throw new ResourceNotFoundException(Messages.RESOURCE_NOT_FOUND);
        }
        repository.deleteById(uuid);
    }

    @Override
    public Category update(Category category) {
        Category aux = repository.findById(category.getUuidCategory()).orElseThrow(
                () -> new ResourceNotFoundException(Messages.RESOURCE_NOT_FOUND)
        );
        aux.setNameCategory(category.getNameCategory());
        aux.setDescCategory(category.getDescCategory());
        aux.setProduct(category.getProduct());
        return repository.save(aux);
    }
}
