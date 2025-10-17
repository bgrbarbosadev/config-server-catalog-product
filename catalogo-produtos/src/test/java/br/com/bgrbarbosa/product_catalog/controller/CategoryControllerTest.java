package br.com.bgrbarbosa.product_catalog.controller;

import br.com.bgrbarbosa.product_catalog.config.Messages;
import br.com.bgrbarbosa.product_catalog.controller.mapper.CategoryMapper;
import br.com.bgrbarbosa.product_catalog.controller.mapper.ProductMapper;
import br.com.bgrbarbosa.product_catalog.model.Category;
import br.com.bgrbarbosa.product_catalog.model.Product;
import br.com.bgrbarbosa.product_catalog.model.dto.CategoryDTO;
import br.com.bgrbarbosa.product_catalog.model.dto.ProductDTO;
import br.com.bgrbarbosa.product_catalog.repository.CategoryRepository;
import br.com.bgrbarbosa.product_catalog.service.EmailService;
import br.com.bgrbarbosa.product_catalog.service.exception.ResourceNotFoundException;
import br.com.bgrbarbosa.product_catalog.service.impl.CategoryServiceImpl;
import br.com.bgrbarbosa.product_catalog.service.impl.ProductServiceImpl;
import br.com.bgrbarbosa.product_catalog.specification.filter.ProductFilter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"HOST_MAIL=smtp.gmail.com", "HOST_NAME=bgrbarbosa@gmail.com",
        "HOST_PASSWORD=pwffnvfhhejcepr", "PORT=587", "SECRET_TOKEN=myapp", "WITH_ISSUER=myapp",
        "DATASOURCE_URL:jdbc:postgresql://localhost:5432/product-catalog-db", "DATASOURCE_USERNAME:postgres",
        "DATASOURCE_PASSWORD:example"})
class CategoryControllerTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CategoryMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @MockBean
    private CategoryServiceImpl service;

    @MockBean
    private EmailService emailService;

    private Category c1;
    private Category c2;
    private List<Category> listCategory;
    private List<CategoryDTO> listCategoryDTO;
    private Category category;

    private PageRequest pageable;
    Page<Category> pageOfCategorys;

    @BeforeEach
    void setUp() {

        c1 = categoryRepository.save(
                new Category(null, "Teste categoria 1", "Teste categoria 1", LocalDate.of(2023, 10, 26),LocalDate.of(2023, 10, 26), List.of()));

        c2 = categoryRepository.save(
                new Category(null, "Teste categoria 2", "Teste categoria 2", LocalDate.of(2023, 10, 26),LocalDate.of(2023, 10, 26), List.of()));

        listCategory = List.of(c1,c2);
        listCategoryDTO = List.of(mapper.parseToDto(c1), mapper.parseToDto(c2));
        pageable =  PageRequest.of(0, 10, Sort.Direction.ASC, "uuidProduct");
        pageOfCategorys = new PageImpl<>(listCategory, pageable, listCategory.size());
    }

    @Test
    @DisplayName("Should return a page with a list containing registered categorys")
    @WithMockUser(roles = {"ADMIN", "USER"})
    void returnPageListWithCategorySuccessfully() throws JsonProcessingException, Exception {

        when(service.findAll(any(Pageable.class))).thenReturn(listCategory);
        Page<CategoryDTO> expectedPageDto = mapper.toPageDTO(listCategoryDTO, pageable);

        // When / Act
        ResultActions response = mockMvc.perform(get("/category")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "uuidCategory,ASC")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(listCategory)));

        // Then / Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(expectedPageDto.getContent().size())))
                .andExpect(jsonPath("$.totalElements", is((int) expectedPageDto.getTotalElements())))
                .andExpect(jsonPath("$.content[0].uuidCategory", is(listCategory.get(0).getUuidCategory().toString())))
                .andExpect(jsonPath("$.content[0].nameCategory", is(listCategory.get(0).getNameCategory().toString())))
                .andExpect(jsonPath("$.content[0].descCategory", is(listCategory.get(0).getDescCategory().toString())))
                .andExpect(jsonPath("$.content[0].dtCreated", is(listCategory.get(0).getDtCreated().toString())))
                .andExpect(jsonPath("$.content[0].dtUpdated", is(listCategory.get(0).getDtUpdated().toString())));
    }

    @Test
    @DisplayName("Must return a category with existing uuid")
    @WithMockUser(roles = {"ADMIN", "USER"})
    void returnACategoryWhenTheUUIDExists() throws JsonProcessingException, Exception {
        UUID uuid = c1.getUuidCategory();
        when(service.findById(uuid)).thenReturn(c1);

        // When / Act
        ResultActions response = mockMvc.perform(get("/category/{uuid}", uuid)
                .contentType(MediaType.APPLICATION_JSON));

        // Then / Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.uuidCategory", is(c1.getUuidCategory().toString())))
                .andExpect(jsonPath("$.nameCategory", is(c1.getNameCategory())))
                .andExpect(jsonPath("$.descCategory", is(c1.getDescCategory())))
                .andExpect(jsonPath("$.dtCreated", is(c1.getDtCreated().toString())))
                .andExpect(jsonPath("$.dtUpdated", is(c1.getDtUpdated().toString())));
    }

    @Test
    @DisplayName("Must return a category with not existing uuid")
    @WithMockUser(roles = {"ADMIN", "USER"})
    void returnACategoryWhenTheUUIDNotExists() throws JsonProcessingException, Exception {
        UUID uuid = UUID.randomUUID();
        when(service.findById(uuid)).thenThrow(new ResourceNotFoundException(Messages.RESOURCE_NOT_FOUND));

        // When / Act
        ResultActions response = mockMvc.perform(get("/category/{uuid}", uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(c1)));

        // Then / Assert
        response.andExpect(status().isNotFound());
    }

    @WithMockUser(roles = "USER")
    @Test
    void testGerarRelatorioPdf() throws Exception {
        // Mock do serviço para retornar dados
        Category mockCategory = new Category();
        mockCategory.setUuidCategory(c1.getUuidCategory());
        mockCategory.setNameCategory("Category Teste");
        List<Category> mockData = Collections.singletonList(mockCategory);
        when(service.findAll()).thenReturn(mockData);

        // Simular a requisição para PDF (padrão)
        MvcResult result = mockMvc.perform(get("/category/report"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"relatorio.pdf\""))
                .andReturn();

        assertNotNull(result.getResponse().getContentAsByteArray());
    }

    @WithMockUser(roles = "USER")
    @Test
    void testGerarRelatorioXlsx() throws Exception {
        Category mockCategory = new Category();
        mockCategory.setUuidCategory(c1.getUuidCategory());
        mockCategory.setNameCategory("Category Teste");
        List<Category> mockData = Collections.singletonList(mockCategory);
        when(service.findAll()).thenReturn(mockData);

        mockMvc.perform(get("/category/report").param("fileType", "xlsx"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"relatorio.xlsx\""));
    }

    @WithMockUser(roles = "USER")
    @Test
    void testGerarRelatorioCsv() throws Exception {

        Category mockCategory = new Category();
        mockCategory.setUuidCategory(c1.getUuidCategory());
        mockCategory.setNameCategory("Category Teste");
        List<Category> mockData = Collections.singletonList(mockCategory);
        when(service.findAll()).thenReturn(mockData);

        // Simular a requisição para CSV
        mockMvc.perform(get("/category/report").param("fileType", "csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"relatorio.csv\""));
    }

    @Test
    @DisplayName("Should insert a new category and return a 201 Created status")
    @WithMockUser(roles = "ADMIN")
    void insertCategorySuccessfully() throws Exception {

        when(service.insert(any(Category.class))).thenReturn(c1);

        String productDtoJson = objectMapper.writeValueAsString(c1);

        mockMvc.perform(post("/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productDtoJson))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuidCategory", is(c1.getUuidCategory().toString())))
                .andExpect(jsonPath("$.nameCategory", is(c1.getNameCategory())))
                .andExpect(jsonPath("$.descCategory", is(c1.getDescCategory())))
                .andExpect(jsonPath("$.dtCreated", is(c1.getDtCreated().toString())))
                .andExpect(jsonPath("$.dtUpdated", is(c1.getDtUpdated().toString())));
    }

    @DisplayName("Should return 400 Bad Request when category data is invalid")
    @WithMockUser(roles = {"ADMIN", "USER"})
    void insertCategoryWithInvalidDataReturnsBadRequest() throws Exception {
        // Given / Arrange
        CategoryDTO invalidCategoryDTO = new CategoryDTO(null, null, "Description of invalid category", LocalDate.of(2025, 9, 10), null);

        // When / Act
        ResultActions response = mockMvc.perform(post("/category")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCategoryDTO)));

        // Then / Assert
        response.andExpect(status().isBadRequest());
        response.andExpect(jsonPath("$.errors").exists());
        response.andExpect(jsonPath("$.errors[?(@.fieldName == 'nameCategory')].message").exists());
    }

    @Test
    @DisplayName("Should update an existing category and return a 200 OK status")
    @WithMockUser(roles = "ADMIN")
    void updateCategorySuccessfully() throws Exception {

        Category updatedCategory = c1;
        updatedCategory.setNameCategory("Categoria atualizada");
        when(service.update(any(Category.class))).thenReturn(updatedCategory);

        CategoryDTO dtoToUpdate = mapper.parseToDto(updatedCategory);

        String dtoAsJson = objectMapper.writeValueAsString(dtoToUpdate);

        mockMvc.perform(put("/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dtoAsJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuidCategory", is(dtoToUpdate.uuidCategory().toString())))
                .andExpect(jsonPath("$.nameCategory", is(dtoToUpdate.nameCategory())))
                .andExpect(jsonPath("$.descCategory", is(dtoToUpdate.descCategory())))
                .andExpect(jsonPath("$.dtCreated", is(dtoToUpdate.dtCreated().toString())))
                .andExpect(jsonPath("$.dtUpdated", is(dtoToUpdate.dtUpdated().toString())));
    }

    @Test
    @DisplayName("Should return 404 Not Found when category to update does not exist")
    @WithMockUser(roles = "ADMIN")
    void updateCategoryNotFound() throws Exception {

        when(service.update(any(Category.class))).thenThrow(new ResourceNotFoundException("Categoria não encontrado."));

        CategoryDTO dtoToUpdate = new CategoryDTO(
                UUID.randomUUID(),
                "Categoria inexistente",
                "Descrição da categoria inexistente",
                LocalDate.of(2023, 10, 26),
                LocalDate.of(2023, 10, 26)
        );

        String dtoAsJson = objectMapper.writeValueAsString(dtoToUpdate);

        mockMvc.perform(put("/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dtoAsJson))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return a \"Bad Request\" for invalid category data when updating.")
    @WithMockUser(roles = {"ADMIN"})
    void shouldReturnBadRequestForInvalidCategoryDataOnUpdate() throws Exception {
        // Given
        CategoryDTO invalidCategoryDTO = new CategoryDTO(
                UUID.randomUUID(),
                "", // Nome da categoria inválido (vazio)
                "Descricao valida",
                LocalDate.now(),
                LocalDate.now()
        );

        // When
        ResultActions response = mockMvc.perform(put("/category")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCategoryDTO)));

        // Then
        response.andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Should delete an existing category and return a 204 No Content status")
    @WithMockUser(roles = "ADMIN")
    void deleteCategorySuccessfully() throws Exception {

        UUID uuidToDelete = c1.getUuidCategory();
        doNothing().when(service).delete(uuidToDelete);

        mockMvc.perform(delete("/category/{uuid}", uuidToDelete))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 404 Not Found when category to delete does not exist")
    @WithMockUser(roles = "ADMIN")
    void deleteCategoryNotFound() throws Exception {

        UUID uuidToDelete = UUID.randomUUID();
        doThrow(new ResourceNotFoundException(Messages.RESOURCE_NOT_FOUND)).when(service).delete(uuidToDelete);

        mockMvc.perform(delete("/category/{uuid}", uuidToDelete))
                .andExpect(status().isNotFound());
    }

    @AfterEach
    void cleanup() {
        categoryRepository.deleteAll(Arrays.asList(c1, c2));
    }

}