package br.com.bgrbarbosa.product_catalog.controller;

import br.com.bgrbarbosa.product_catalog.config.Messages;
import br.com.bgrbarbosa.product_catalog.controller.mapper.ProductMapper;
import br.com.bgrbarbosa.product_catalog.model.Category;
import br.com.bgrbarbosa.product_catalog.model.Product;
import br.com.bgrbarbosa.product_catalog.model.Role;
import br.com.bgrbarbosa.product_catalog.model.User;
import br.com.bgrbarbosa.product_catalog.model.dto.CategoryDTO;
import br.com.bgrbarbosa.product_catalog.model.dto.ProductDTO;
import br.com.bgrbarbosa.product_catalog.model.dto.UserRequestDTO;
import br.com.bgrbarbosa.product_catalog.repository.CategoryRepository;
import br.com.bgrbarbosa.product_catalog.repository.ProductRepository;
import br.com.bgrbarbosa.product_catalog.service.EmailService;
import br.com.bgrbarbosa.product_catalog.service.exception.ResourceNotFoundException;
import br.com.bgrbarbosa.product_catalog.service.impl.ProductServiceImpl;
import br.com.bgrbarbosa.product_catalog.service.impl.UserServiceImpl;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"HOST_MAIL=smtp.gmail.com", "HOST_NAME=bgrbarbosa@gmail.com",
        "HOST_PASSWORD=pwffnvfhhejcepr", "PORT=587", "SECRET_TOKEN=myapp", "WITH_ISSUER=myapp",
        "DATASOURCE_URL:jdbc:postgresql://localhost:5432/product-catalog-db", "DATASOURCE_USERNAME:postgres",
        "DATASOURCE_PASSWORD:example"})
class ProductControllerTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ProductMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @MockBean
    private ProductServiceImpl service;

    @MockBean
    private EmailService emailService;

    private Product p1;
    private Product p2;
    private List<Product> listProduct;
    private List<ProductDTO> listProductDTO;
    private Category category;

    private PageRequest pageable;
    Page<Product> pageOfProducts;
    private ProductFilter filter;

    @BeforeEach
    void setUp() {

        category = categoryRepository.save(
                new Category(null, "Cabos", "Categoria de cabos", LocalDate.of(2023, 10, 26),LocalDate.of(2023, 10, 26), List.of()));

        p1 = productRepository.save(
                new Product(null, "Cabo de Rede par trançado", "Cabo de rede par trançado categoria 5e Furukawa", 200.0, "http://upload123", LocalDate.of(2023, 10, 26), null, category));
        p2 = productRepository.save(
                new Product(null, "Cabo de celular V8", "Cabo de celular V8", 20.0, "http://upload321", LocalDate.of(2023, 10, 26), null, category));

        listProduct = List.of(p1,p2);
        listProductDTO = List.of(mapper.parseToDto(p1), mapper.parseToDto(p2));
        pageable =  PageRequest.of(0, 10, Sort.Direction.ASC, "uuidProduct");
        pageOfProducts = new PageImpl<>(listProduct, pageable, listProduct.size());
        filter = new ProductFilter(null, null, null, category.getNameCategory());
    }

    @Test
    @DisplayName("Should return a page with a list containing registered products")
    @WithMockUser(roles = {"ADMIN", "USER"})
    void returnPageListWithProductSuccessfully() throws JsonProcessingException, Exception {

        when(service.findAll(any(Pageable.class), any(ProductFilter.class))).thenReturn(listProduct);
        Page<ProductDTO> expectedPageDto = mapper.toPageDTO(listProductDTO, pageable);

        // When / Act
        ResultActions response = mockMvc.perform(get("/product")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "uuidProduct,ASC")
                .param("categoryName", category.getNameCategory())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(listProduct)));

        // Then / Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(expectedPageDto.getContent().size())))
                .andExpect(jsonPath("$.totalElements", is((int) expectedPageDto.getTotalElements())))
                .andExpect(jsonPath("$.content[0].uuidProduct", is(listProduct.get(0).getUuidProduct().toString())))
                .andExpect(jsonPath("$.content[0].nameProduct", is(listProduct.get(0).getNameProduct())))
                .andExpect(jsonPath("$.content[0].descriptionProduct", is(listProduct.get(0).getDescriptionProduct())))
                .andExpect(jsonPath("$.content[0].priceProduct", is(listProduct.get(0).getPriceProduct())))
                .andExpect(jsonPath("$.content[0].urlProduct", is(listProduct.get(0).getUrlProduct())))
                .andExpect(jsonPath("$.content[0].dtCreated", is(listProduct.get(0).getDtCreated().toString())))
                .andExpect(jsonPath("$.content[0].dtUpdated", is(listProduct.get(0).getDtUpdated())))
                .andExpect(jsonPath("$.content[0].categoryProduct.uuidCategory", is(listProduct.get(0).getCategoryProduct().getUuidCategory().toString())))
                .andExpect(jsonPath("$.content[0].categoryProduct.nameCategory", is(listProduct.get(0).getCategoryProduct().getNameCategory())))
                .andExpect(jsonPath("$.content[0].categoryProduct.descCategory", is(listProduct.get(0).getCategoryProduct().getDescCategory())))
                .andExpect(jsonPath("$.content[0].categoryProduct.dtCreated", is(listProduct.get(0).getCategoryProduct().getDtCreated().toString())))
                .andExpect(jsonPath("$.content[0].categoryProduct.dtUpdated", is(listProduct.get(0).getCategoryProduct().getDtUpdated().toString())));
    }

    @Test
    @DisplayName("Must return a product with existing uuid")
    @WithMockUser(roles = {"ADMIN", "USER"})
    void returnAProductWhenTheUUIDExists() throws JsonProcessingException, Exception {
        UUID uuid = p1.getUuidProduct();
        when(service.findById(uuid)).thenReturn(p1);

        // When / Act
        ResultActions response = mockMvc.perform(get("/product/{uuid}", uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(p1)));

        // Then / Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.uuidProduct", is(p1.getUuidProduct().toString())))
                .andExpect(jsonPath("$.nameProduct", is(p1.getNameProduct())))
                .andExpect(jsonPath("$.descriptionProduct", is(p1.getDescriptionProduct())))
                .andExpect(jsonPath("$.priceProduct", is(p1.getPriceProduct())))
                .andExpect(jsonPath("$.urlProduct", is(p1.getUrlProduct())))
                .andExpect(jsonPath("$.dtCreated", is(p1.getDtCreated().toString())))
                .andExpect(jsonPath("$.dtUpdated", is(p1.getDtUpdated())))
                .andExpect(jsonPath("$.categoryProduct.uuidCategory", is(p1.getCategoryProduct().getUuidCategory().toString())))
                .andExpect(jsonPath("$.categoryProduct.nameCategory", is(p1.getCategoryProduct().getNameCategory())))
                .andExpect(jsonPath("$.categoryProduct.descCategory", is(p1.getCategoryProduct().getDescCategory())))
                .andExpect(jsonPath("$.categoryProduct.dtCreated", is(p1.getCategoryProduct().getDtCreated().toString())))
                .andExpect(jsonPath("$.categoryProduct.dtUpdated", is(p1.getCategoryProduct().getDtUpdated().toString())));
    }

    @Test
    @DisplayName("Must return a product with not existing uuid")
    @WithMockUser(roles = {"ADMIN", "USER"})
    void returnAProductWhenTheUUIDNotExists() throws JsonProcessingException, Exception {
        UUID uuid = UUID.randomUUID();
        when(service.findById(uuid)).thenThrow(new ResourceNotFoundException(Messages.RESOURCE_NOT_FOUND));

        // When / Act
        ResultActions response = mockMvc.perform(get("/product/{uuid}", uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(p1)));

        // Then / Assert
        response.andExpect(status().isNotFound());
    }

    @WithMockUser(roles = "USER")
    @Test
    void testGerarRelatorioPdf() throws Exception {
        // Mock do serviço para retornar dados
        Product mockProduct = new Product();
        mockProduct.setUuidProduct(p1.getUuidProduct());
        mockProduct.setNameProduct("Produto Teste");
        List<Product> mockData = Collections.singletonList(mockProduct);
        when(service.findAll(new ProductFilter())).thenReturn(mockData);

        // Simular a requisição para PDF (padrão)
        MvcResult result = mockMvc.perform(get("/product/report"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"relatorio.pdf\""))
                .andReturn();

        // Opcional: verificar se o corpo da resposta não está vazio
        assertNotNull(result.getResponse().getContentAsByteArray());
    }

    @WithMockUser(roles = "USER")
    @Test
    void testGerarRelatorioXlsx() throws Exception {
        // Mock do serviço
        Product mockProduct = new Product();
        mockProduct.setUuidProduct(p1.getUuidProduct());
        mockProduct.setNameProduct("Produto Teste");
        List<Product> mockData = Collections.singletonList(mockProduct);
        when(service.findAll(new ProductFilter())).thenReturn(mockData);

        // Simular a requisição para XLSX
        mockMvc.perform(get("/product/report").param("fileType", "xlsx"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"relatorio.xlsx\""));
    }

    @WithMockUser(roles = "USER")
    @Test
    void testGerarRelatorioCsv() throws Exception {
        // Mock do serviço
        Product mockProduct = new Product();
        mockProduct.setUuidProduct(p1.getUuidProduct());
        mockProduct.setNameProduct("Produto Teste");
        List<Product> mockData = Collections.singletonList(mockProduct);
        when(service.findAll(new ProductFilter())).thenReturn(mockData);

        // Simular a requisição para CSV
        mockMvc.perform(get("/product/report").param("fileType", "csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"relatorio.csv\""));
    }

    @Test
    @DisplayName("Should insert a new product and return a 201 Created status")
    @WithMockUser(roles = "ADMIN")
    void insertProductSuccessfully() throws Exception {

        when(service.insert(any(Product.class))).thenReturn(p1);

        String productDtoJson = objectMapper.writeValueAsString(p1);

        mockMvc.perform(post("/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productDtoJson))
                .andDo(print()) // Adiciona um print no console para facilitar a depuração
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.uuidProduct", is(p1.getUuidProduct().toString())))
                .andExpect(jsonPath("$.nameProduct", is(p1.getNameProduct())))
                .andExpect(jsonPath("$.descriptionProduct", is(p1.getDescriptionProduct())))
                .andExpect(jsonPath("$.priceProduct", is(p1.getPriceProduct())))
                .andExpect(jsonPath("$.urlProduct", is(p1.getUrlProduct())))
                .andExpect(jsonPath("$.categoryProduct.uuidCategory", is(p1.getCategoryProduct().getUuidCategory().toString())));

    }

    @Test
    @DisplayName("Should return a \"Bad Request\" for invalid product data when inserting.")
    @WithMockUser(roles = {"ADMIN"})
    void shouldReturnBadRequestForInvalidProductDataOnInsert() throws Exception {
        // Given
        ProductDTO invalidProductDTO = new ProductDTO(
                UUID.randomUUID(),
                "",
                "",
                20.00,
                "",
                null,
                null,
                null
        );

        // When
        ResultActions response = mockMvc.perform(post("/product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProductDTO)));

        // Then
        response.andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Should return a success message when the email is sent.")
    @WithMockUser(roles = "USER")
    void enviarEmailComSucesso() throws Exception {

        String destinoEmail = "teste@exemplo.com";
        doNothing().when(emailService).sendingProductListByEmail(destinoEmail);

        mockMvc.perform(post("/product/enviar-email")
                        .param("destination", destinoEmail))
                .andExpect(status().isOk())
                .andExpect(content().string("E-mail enviado com sucesso!"));
    }

    @Test
    @DisplayName("Should return error message when email sending fails")
    @WithMockUser(roles = "USER")
    void enviarEmailComFalha() throws Exception {

        String destinoEmail = "teste_falha@exemplo.com";
        doThrow(new RuntimeException("Simulando erro de envio")).when(emailService).sendingProductListByEmail(destinoEmail);

        mockMvc.perform(post("/product/enviar-email")
                        .param("destination", destinoEmail))
                .andExpect(status().isOk())
                .andExpect(content().string("Erro ao enviar e-mail: Simulando erro de envio"));
    }

    @Test
    @DisplayName("Should update an existing product and return a 200 OK status")
    @WithMockUser(roles = "ADMIN")
    void updateProductSuccessfully() throws Exception {

        Product updatedProduct = p1;
        updatedProduct.setNameProduct("Cabo de Rede ATUALIZADO");
        updatedProduct.setPriceProduct(500.0);
        when(service.update(any(Product.class))).thenReturn(updatedProduct);

        ProductDTO dtoToUpdate = mapper.parseToDto(updatedProduct);

        String dtoAsJson = objectMapper.writeValueAsString(dtoToUpdate);

        mockMvc.perform(put("/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dtoAsJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuidProduct", is(dtoToUpdate.uuidProduct().toString())))
                .andExpect(jsonPath("$.nameProduct", is("Cabo de Rede ATUALIZADO")))
                .andExpect(jsonPath("$.descriptionProduct", is(dtoToUpdate.descriptionProduct())))
                .andExpect(jsonPath("$.priceProduct", is(500.0)))
                .andExpect(jsonPath("$.urlProduct", is(dtoToUpdate.urlProduct())));
    }

    @Test
    @DisplayName("Should return a \"Bad Request\" for invalid product data when updating.")
    @WithMockUser(roles = {"ADMIN"})
    void shouldReturnBadRequestForInvalidProductDataOnUpdate() throws Exception {
        // Given
        ProductDTO invalidProductDTO = new ProductDTO(
                UUID.randomUUID(),
                "",
                "",
                20.00,
                "",
                null,
                null,
                null
        );

        // When
        ResultActions response = mockMvc.perform(put("/product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProductDTO)));

        // Then
        response.andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Should return 404 Not Found when product to update does not exist")
    @WithMockUser(roles = "ADMIN")
    void updateProductNotFound() throws Exception {

        when(service.update(any(Product.class))).thenThrow(new ResourceNotFoundException("Produto não encontrado."));

        ProductDTO dtoToUpdate = new ProductDTO(
                UUID.randomUUID(),
                "Produto Inexistente",
                "Descrição do produto inexistente",
                100.0,
                "http://naoexiste.com",
                LocalDate.of(2023, 10, 26),
                null,
                category
        );

        String dtoAsJson = objectMapper.writeValueAsString(dtoToUpdate);

        mockMvc.perform(put("/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dtoAsJson))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should delete an existing product and return a 204 No Content status")
    @WithMockUser(roles = "ADMIN")
    void deleteProductSuccessfully() throws Exception {

        UUID uuidToDelete = p1.getUuidProduct();
        doNothing().when(service).delete(uuidToDelete);

        mockMvc.perform(delete("/product/{uuid}", uuidToDelete))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 404 Not Found when product to delete does not exist")
    @WithMockUser(roles = "ADMIN")
    void deleteProductNotFound() throws Exception {

        UUID uuidToDelete = UUID.randomUUID();
        doThrow(new ResourceNotFoundException(Messages.RESOURCE_NOT_FOUND)).when(service).delete(uuidToDelete);

        mockMvc.perform(delete("/product/{uuid}", uuidToDelete))
                .andExpect(status().isNotFound());
    }



    @AfterEach
    void cleanup() {
        productRepository.deleteAll(Arrays.asList(p1, p2));
        categoryRepository.delete(this.category);
    }
}