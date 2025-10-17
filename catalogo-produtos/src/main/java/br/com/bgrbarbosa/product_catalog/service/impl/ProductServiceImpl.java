package br.com.bgrbarbosa.product_catalog.service.impl;

import br.com.bgrbarbosa.product_catalog.config.Messages;
import br.com.bgrbarbosa.product_catalog.model.Product;
import br.com.bgrbarbosa.product_catalog.repository.ProductRepository;
import br.com.bgrbarbosa.product_catalog.service.ProductService;
import br.com.bgrbarbosa.product_catalog.service.exception.ResourceNotFoundException;
import br.com.bgrbarbosa.product_catalog.specification.filter.ProductFilter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    @Override
    @Transactional
    public Product insert(Product product) {
        return repository.save(product);
    }

    @Override
    public List<Product> findAll(Pageable page, ProductFilter filter) {
        return repository.findAll(filter.toSpecification());
    }

    @Override
    public List<Product> findAll(ProductFilter filter) {
        return repository.findAll(filter.toSpecification());
    }

    @Override
    public List<Product> findAll() {
        return repository.findAll();
    }

    @Override
    public Product findById(UUID uuid) {
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
    public Product update(Product product) {
        Product aux = repository.findById(product.getUuidProduct()).orElseThrow(
                () -> new ResourceNotFoundException(Messages.RESOURCE_NOT_FOUND)
        );
        aux.setNameProduct(product.getNameProduct());
        aux.setDescriptionProduct(product.getDescriptionProduct());
        aux.setPriceProduct(product.getPriceProduct());
        aux.setUrlProduct(product.getUrlProduct());
        aux.setCategoryProduct(product.getCategoryProduct());
        return repository.save(aux);
    }
}
