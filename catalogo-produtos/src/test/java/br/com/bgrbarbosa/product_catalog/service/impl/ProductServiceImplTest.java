package br.com.bgrbarbosa.product_catalog.service.impl;

import br.com.bgrbarbosa.product_catalog.config.Messages;
import br.com.bgrbarbosa.product_catalog.model.Category;
import br.com.bgrbarbosa.product_catalog.model.Product;
import br.com.bgrbarbosa.product_catalog.repository.ProductRepository;
import br.com.bgrbarbosa.product_catalog.service.exception.ResourceNotFoundException;
import br.com.bgrbarbosa.product_catalog.service.exception.UserException;
import br.com.bgrbarbosa.product_catalog.specification.ProductSpec;
import br.com.bgrbarbosa.product_catalog.specification.filter.ProductFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {
    @Mock
    private ProductRepository repository;

    @Mock
    private ProductSpec spec;

    @InjectMocks
    private ProductServiceImpl service;

    private Product p1;
    private Product p2;
    private Product productUpdate;
    private List<Product> listProduct;
    private Category category;
    private ProductFilter filter;
    UUID uuidP1 = UUID.randomUUID();
    UUID uuidP2 = UUID.randomUUID();
    UUID uuidCategory = UUID.randomUUID();
    UUID uuidNotExist = UUID.randomUUID();

    PageRequest pageable;

    @BeforeEach
    void setUp() {
        category = new Category(UUID.randomUUID(), "Cabos", "Categoria de cabos", LocalDate.of(2023, 10, 26),LocalDate.of(2023, 10, 26), List.of());
        p1 = new Product(uuidP1, "Cabo de Rede par trançado", "Cabo de rede par trançado categoria 5e Furukawa", 200.0, "http://upload123", LocalDate.of(2023, 10, 26), null, category);
        p2 = new Product(uuidP2, "Cabo de celular V8", "Cabo de celular V8", 20.0, "http://upload321", LocalDate.of(2023, 10, 26), null, category);
        productUpdate = new Product(uuidP2, "Cabo de celular V8", "Cabo de celular V8", 20.0, "http://upload321", LocalDate.of(2023, 10, 26), null, category);
        listProduct = List.of(p1,p2);
        pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "uuid");
        filter = new ProductFilter(null, null, null, category.getNameCategory());
    }

    @Test
    @DisplayName("You must insert a product successfully")
    void insertProductSuccessfully() throws UserException {
        //Given / Arrange
        when(repository.save(any(Product.class))).thenReturn(p1);

        // When / Act
        Product result = service.insert(p1);

        // Then / Assert
        verify(repository, times(1)).save(p1);
        assertEquals(result.getClass(), p1.getClass());
        assertEquals(result.getUuidProduct(), p1.getUuidProduct());
        assertEquals(result.getNameProduct(), p1.getNameProduct());
        assertEquals(result.getPriceProduct(), p1.getPriceProduct());
        assertEquals(result.getUrlProduct(), p1.getUrlProduct());
        assertEquals(result.getDescriptionProduct(), p1.getDescriptionProduct());
        assertEquals(result.getCategoryProduct(), p1.getCategoryProduct());
    }

    @Test
    @DisplayName("Must return a list of product")
    void ReturnsAListOfProductPageFilter() {
        // Given / Arrange
        when(repository.findAll(any(Specification.class)))
                .thenReturn(listProduct);

        // When / Act
        List<Product> result = service.findAll(pageable, filter);

        // Then / Assert
        assertNotNull(result);
        assertEquals(2, result.size());

    }

    @Test
    @DisplayName("Must return a list of product")
    void ReturnsAListOfProductFilter() {
        // Given / Arrange
        Mockito.when(repository.findAll(ArgumentMatchers.any(Specification.class))).thenReturn(listProduct);

        // When / Act
        List<Product> result = service.findAll(filter);

        // Then / Assert
        Assertions.assertEquals(result.size(), 2);
        Assertions.assertEquals(result.get(0).getClass(), Product.class);
    }

    @Test
    @DisplayName("Must return a list of product")
    void ReturnsAListOfProduct() {
        // Given / Arrange
        Mockito.when(repository.findAll()).thenReturn(listProduct);

        // When / Act
        List<Product> result = service.findAll();

        // Then / Assert
        Assertions.assertEquals(result.size(), 2);
        Assertions.assertEquals(result.get(0).getClass(), Product.class);
    }

    @Test
    @DisplayName("Must return a product when id exists")
    void findByIdWhenIdExist() {
        // Given / Arrange
        Mockito.when(repository.findById(any())).thenReturn(Optional.ofNullable(p1));

        // When / Act
        Product result = service.findById(any());

        // Then / Assert
        verify(repository, times(1)).findById(any());
        assertEquals(result.getClass(), Product.class);
        assertEquals(result.getClass(), p1.getClass());
        assertEquals(result.getUuidProduct(), p1.getUuidProduct());
        assertEquals(result.getNameProduct(), p1.getNameProduct());
        assertEquals(result.getDescriptionProduct(), p1.getDescriptionProduct());
        assertEquals(result.getUrlProduct(), p1.getUrlProduct());
        assertEquals(result.getPriceProduct(), p1.getPriceProduct());

    }

    @Test
    @DisplayName("Must not return a product if the id does not exist")
    void findByIdWhenIdNotExists() throws ResourceNotFoundException {
        //Given / Arrange
        when(repository.findById(uuidNotExist))
                .thenReturn(Optional.empty());

        // When / Act
        assertThrows(ResourceNotFoundException.class, () -> {
            service.findById(uuidNotExist);
        }, Messages.RESOURCE_NOT_FOUND);

    }

    @Test
    @DisplayName("Should delete product when id exists")
    void deleteByIdWhenIdExists() throws ResourceNotFoundException {
        // Given / Arrange
        when(repository.existsById(any())).thenReturn(true);

        // When / Act
        service.delete(uuidP1);

        // Then / Assert
        verify(repository, times(1)).deleteById(uuidP1);

    }

    @Test
    @DisplayName("Must not return a product if the id does not exist")
    void deleteByIdWhenIdNotExists() throws UserException {
        // When / Act
        assertThrows(ResourceNotFoundException.class, () -> {
            service.delete(uuidNotExist);
        }, Messages.RESOURCE_NOT_FOUND);

    }

    @Test
    @DisplayName("Must update a product when it exists")
    void whenUpdateProductThenReturnsUpdatedCategory() {
        // Given
        when(repository.findById(p2.getUuidProduct())).thenReturn(Optional.of(p2));
        when(repository.save(any(Product.class))).thenReturn(productUpdate);

        // When
        Product result = service.update(p2);

        // Then
        assertNotNull(result);
        assertEquals(result.getClass(), Product.class);
        assertEquals(result.getClass(), p2.getClass());
        assertEquals(result.getUuidProduct(), p2.getUuidProduct());
        assertEquals(result.getNameProduct(), p2.getNameProduct());
        assertEquals(result.getDescriptionProduct(), p2.getDescriptionProduct());
        assertEquals(result.getUrlProduct(), p2.getUrlProduct());
        assertEquals(result.getPriceProduct(), p2.getPriceProduct());

        verify(repository, times(1)).findById(p2.getUuidProduct());
        verify(repository, times(1)).save(p2);
    }

    @Test
    @DisplayName("You should not update a product when it does not exist.")
    void whenUpdateProductNotFoundThenThrowsResourceNotFoundException() {
        // Given
        when(repository.findById(any())).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> service.update(p1));

        verify(repository, times(1)).findById(p1.getUuidProduct());
        verify(repository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should set dtCreated date before persisting")
    void prePersistShouldSetDtCreated() {
        // Given
        Product product = new Product();
        product.setUuidProduct(UUID.randomUUID());
        product.setNameProduct("Product pre persiste");
        product.setDescriptionProduct("Description");
        product.setPriceProduct(200.00);
        product.setUrlProduct("http://url");
        product.setCategoryProduct(category);

        assertNull(product.getDtCreated(), "dtCreated should be null before calling prePersist");

        // When
        product.prePersist();

        // Then
        assertNotNull(product.getDtCreated(), "dtCreated should not be null after prePersist");
        assertEquals(LocalDate.now(), product.getDtCreated(), "dtCreated should be equal to the current date");
    }

    @Test
    @DisplayName("Should set dtUpdated date before updating")
    void preUpdateShouldSetDtUpdated() {
        // Given
        Product product = new Product();
        product.setUuidProduct(UUID.randomUUID());
        product.setNameProduct("Product pre persiste");
        product.setDescriptionProduct("Description");
        product.setPriceProduct(200.00);
        product.setUrlProduct("http://url");
        product.setCategoryProduct(category);

        assertNull(product.getDtUpdated(), "dtUpdated should be null before calling preUpdate");

        // When
        product.preUpdate();

        // Then
        assertNotNull(product.getDtUpdated(), "dtUpdated should not be null after preUpdate");
        assertEquals(LocalDate.now(), product.getDtUpdated(), "dtUpdated should be equal to the current date");
    }
}