package br.com.bgrbarbosa.product_catalog.service.impl;

import br.com.bgrbarbosa.product_catalog.config.Messages;
import br.com.bgrbarbosa.product_catalog.model.Category;
import br.com.bgrbarbosa.product_catalog.model.Role;
import br.com.bgrbarbosa.product_catalog.model.User;
import br.com.bgrbarbosa.product_catalog.repository.CategoryRepository;
import br.com.bgrbarbosa.product_catalog.service.exception.IllegalArgumentException;
import br.com.bgrbarbosa.product_catalog.service.exception.ResourceNotFoundException;
import br.com.bgrbarbosa.product_catalog.service.exception.UserException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository repository;

    @InjectMocks
    private CategoryServiceImpl service;


    private Category category;
    private Category category2;
    private Category categoryUpdate;
    private List<Category> listCategory;

    UUID uuidCategory = UUID.randomUUID();
    UUID uuidCategory2 = UUID.randomUUID();
    UUID uuidNotExist = UUID.randomUUID();

    PageRequest pageable;

    @BeforeEach
    void setUp() {

        category = new Category(uuidCategory, "Eletronicos", "Categoria de eletrônicos", LocalDate.of(2023, 10, 26),LocalDate.of(2023, 10, 26), List.of());
        category2 = new Category(uuidCategory2, "Eletronicos 2", "Categoria de eletrônicos 2", LocalDate.of(2023, 10, 26),LocalDate.of(2023, 10, 26), List.of());
        categoryUpdate = new Category(uuidCategory, "Eletronicos", "Categoria de eletrônicos", LocalDate.of(2023, 10, 26),LocalDate.of(2023, 10, 26), List.of());

        listCategory = List.of(category, category2);

        pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "uuid");
    }

    @Test
    @DisplayName("You must insert a category successfully")
    void insertCategorySuccessfully() throws UserException {
        //Given / Arrange
        when(repository.save(category)).thenReturn(category);

        // When / Act
        Category result = service.insert(category);

        // Then / Assert
        verify(repository, times(1)).save(category);
        assertEquals(result.getClass(), category.getClass());
        assertEquals(result.getUuidCategory(), category.getUuidCategory());
        assertEquals(result.getNameCategory(), category.getNameCategory());
        assertEquals(result.getDescCategory(), category.getDescCategory());
        assertEquals(result.getProduct(), category.getProduct());
        assertEquals(result.getDtCreated(), category.getDtCreated());
        assertEquals(result.getDtUpdated(), category.getDtUpdated());
    }

    @Test
    @DisplayName("Do not insert a user if the email already exists.")
    void insertCategoryNotSuccessfully() throws UserException {
        //Given / Arrange
        Mockito.when(repository.existsByNameCategory(category.getNameCategory())).thenReturn(true);

        // When / Act
        assertThrows(IllegalArgumentException.class, () -> {
            service.insert(category);
        }, Messages.Existing_User);

    }

    @Test
    @DisplayName("Must return a list of category")
    void ReturnsAListOfCategoryPageble() {
        // Given / Arrange
        Mockito.when(repository.findAll()).thenReturn(listCategory);

        // When / Act
        List<Category> result = service.findAll(pageable);

        // Then / Assert
        Assertions.assertEquals(result.size(), 2);
        Assertions.assertEquals(result.get(0).getClass(), Category.class);
    }

    @Test
    @DisplayName("Must return a list of category")
    void ReturnsAListOfCategory() {
        // Given / Arrange
        Mockito.when(repository.findAll()).thenReturn(listCategory);

        // When / Act
        List<Category> result = service.findAll();

        // Then / Assert
        Assertions.assertEquals(result.size(), 2);
        Assertions.assertEquals(result.get(0).getClass(), Category.class);
    }

    @Test
    @DisplayName("Must return a category when id exists")
    void findByIdWhenIdExist() {
        // Given / Arrange
        Mockito.when(repository.findById(any())).thenReturn(Optional.ofNullable(category));

        // When / Act
        Category result = service.findById(any());

        // Then / Assert
        verify(repository, times(1)).findById(any());
        assertEquals(result.getClass(), Category.class);
        assertEquals(result.getUuidCategory(), category.getUuidCategory());
        assertEquals(result.getNameCategory(), category.getNameCategory());
        assertEquals(result.getDescCategory(), category.getDescCategory());
        assertEquals(result.getProduct(), category.getProduct());
        assertEquals(result.getDtCreated(), category.getDtCreated());
        assertEquals(result.getDtUpdated(), category.getDtUpdated());

    }

    @Test
    @DisplayName("Must not return a category if the id does not exist")
    void findByIdWhenIdNotExists() throws ResourceNotFoundException {
        //Given / Arrange
        when(repository.findById(uuidNotExist))
                .thenReturn(Optional.empty());

        // When / Act

        ResourceNotFoundException thrown = assertThrows(
                ResourceNotFoundException.class,
                () -> service.findById(uuidNotExist),
                Messages.RESOURCE_NOT_FOUND
        );

        assertThrows(ResourceNotFoundException.class, () -> {
            service.findById(uuidNotExist);
        }, Messages.RESOURCE_NOT_FOUND);

    }

    @Test
    @DisplayName("Should delete category when id exists")
    void deleteByIdWhenIdExists() throws ResourceNotFoundException {
        // Given / Arrange
        when(repository.existsById(uuidCategory)).thenReturn(true);

        // When / Act
        service.delete(uuidCategory);

        // Then / Assert
        verify(repository, times(1)).deleteById(uuidCategory);

    }

    @Test
    @DisplayName("Must not return a category if the id does not exist")
    void deleteByIdWhenIdNotExists() throws UserException {
        // When / Act
        assertThrows(ResourceNotFoundException.class, () -> {
            service.delete(any());
        }, Messages.RESOURCE_NOT_FOUND);

    }

    @Test
    @DisplayName("Must update a category when it exists")
    void whenUpdateCategoryThenReturnsUpdatedCategory() {
        // Given
        when(repository.findById(category.getUuidCategory())).thenReturn(Optional.of(category));
        when(repository.save(any(Category.class))).thenReturn(categoryUpdate);

        // When
        Category result = service.update(category);

        // Then
        assertNotNull(result);
        assertEquals(result.getClass(), Category.class);
        assertEquals(result.getClass(), category.getClass());
        assertEquals(result.getUuidCategory(), category.getUuidCategory());
        assertEquals(result.getNameCategory(), category.getNameCategory());
        assertEquals(result.getDescCategory(), category.getDescCategory());
        assertEquals(result.getProduct(), category.getProduct());

        verify(repository, times(1)).findById(category.getUuidCategory());
        verify(repository, times(1)).save(category);
    }

    @Test
    @DisplayName("You should not update a category when it does not exist.")
    void whenUpdateCategoryNotFoundThenThrowsResourceNotFoundException() {
        // Given
        when(repository.findById(category.getUuidCategory())).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> service.update(category));

        verify(repository, times(1)).findById(category.getUuidCategory());
        verify(repository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Should set dtCreated date before persisting")
    void prePersistShouldSetDtCreated() {
        // Given
        Category category = new Category();
        category.setUuidCategory(UUID.randomUUID());
        category.setNameCategory("Eletronicos");
        category.setDescCategory("Categoria de eletrônicos");
        category.setProduct(Collections.emptyList());

        assertNull(category.getDtCreated(), "dtCreated should be null before calling prePersist");

        // When
        category.prePersist();

        // Then
        assertNotNull(category.getDtCreated(), "dtCreated should not be null after prePersist");
        assertEquals(LocalDate.now(), category.getDtCreated(), "dtCreated should be equal to the current date");
    }

    @Test
    @DisplayName("Should set dtUpdated date before updating")
    void preUpdateShouldSetDtUpdated() {
        // Given
        Category category = new Category();
        category.setUuidCategory(UUID.randomUUID());
        category.setNameCategory("Eletronicos");
        category.setDescCategory("Categoria de eletrônicos");
        category.setProduct(Collections.emptyList());
        category.prePersist(); // Simulating a creation first

        assertNull(category.getDtUpdated(), "dtUpdated should be null before calling preUpdate");

        // When
        category.preUpdate();

        // Then
        assertNotNull(category.getDtUpdated(), "dtUpdated should not be null after preUpdate");
        assertEquals(LocalDate.now(), category.getDtUpdated(), "dtUpdated should be equal to the current date");
    }
}