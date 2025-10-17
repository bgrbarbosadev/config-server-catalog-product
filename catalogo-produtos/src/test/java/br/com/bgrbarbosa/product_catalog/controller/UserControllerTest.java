package br.com.bgrbarbosa.product_catalog.controller;

import br.com.bgrbarbosa.product_catalog.config.Messages;
import br.com.bgrbarbosa.product_catalog.model.Role;
import br.com.bgrbarbosa.product_catalog.model.User;
import br.com.bgrbarbosa.product_catalog.model.dto.UserRequestDTO;
import br.com.bgrbarbosa.product_catalog.model.dto.UserResponseDTO;
import br.com.bgrbarbosa.product_catalog.repository.RoleRepository;
import br.com.bgrbarbosa.product_catalog.repository.UserRepository;
import br.com.bgrbarbosa.product_catalog.security.TokenService;
import br.com.bgrbarbosa.product_catalog.service.exception.ResourceNotFoundException;
import br.com.bgrbarbosa.product_catalog.service.exception.UserException;
import br.com.bgrbarbosa.product_catalog.service.impl.UserServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"HOST_MAIL=smtp.gmail.com", "HOST_NAME=bgrbarbosa@gmail.com",
        "HOST_PASSWORD=pwffnvfhhejcepr", "PORT=587", "SECRET_TOKEN=myapp", "WITH_ISSUER=myapp",
        "DATASOURCE_URL:jdbc:postgresql://localhost:5432/product-catalog-db", "DATASOURCE_USERNAME:postgres",
        "DATASOURCE_PASSWORD:example"})
class UserControllerTest {

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @MockBean
    private BCryptPasswordEncoder passwordEncoder;

    @MockBean
    private UserServiceImpl service;

    @MockBean
    private TokenService tokenService;

    private User admin;
    private User user;
    private User userDelete;
    private User userUpdate;
    private Role roleUser;
    private Role roleAdmin;
    private List<User> userList;

    private UserRequestDTO userRequestDTO;
    private UserRequestDTO adminRequestDTO;

    private PageRequest pageable;


    @BeforeEach
    void setUp() {
        roleAdmin = new Role(null, "ROLE_ADMIN");
        roleUser = new Role(null, "ROLE_USER");
        roleRepository.saveAll(Arrays.asList(roleAdmin, roleUser));

        this.admin = new User(null, "teste.admin", "teste.admin",     "teste.admin@gmail.com", passwordEncoder.encode("123456"), Set.of(roleUser, roleAdmin));
        this.user =  new User(null, "teste.user",  "teste.user",      "teste.user@gmail.com",  passwordEncoder.encode("123456"), Set.of(roleUser));
        this.userDelete = new User(null, "user delete", "user delete","teste.delete@gmail.com", passwordEncoder.encode("123456"), Set.of(roleUser, roleAdmin));
        this.userUpdate = userRepository.save(new User(null, "teste.admin", "teste.admin",     "teste.admin@gmail.com", passwordEncoder.encode("123456"), Set.of(roleUser, roleAdmin)));
        userRepository.saveAll(Arrays.asList(admin,user,userDelete, userUpdate));

        this.userList = List.of(admin, user);

        this.adminRequestDTO = new UserRequestDTO( "teste.admin@gmail.com", "123456");
        this.userRequestDTO  = new UserRequestDTO( "teste.user@gmail.com",  "123456");

        this.pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "uuid");

    }

    @Test
    @DisplayName("Should return a list containing registered users")
    @WithMockUser(roles = {"ADMIN", "USER"})
    void returnListWithUserSuccessfully() throws JsonProcessingException, Exception {

        // Given / Arrange
        when(service.findAll(pageable)).thenReturn(this.userList);

        // When / Act
        ResultActions response = mockMvc.perform(get("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(userList)));

        // Then / Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(userList.size())))
                .andExpect(jsonPath("$.totalElements", is(userList.size())))
                .andExpect(jsonPath("$.content[0].uuid", is(userList.get(0).getUuid().toString())))
                .andExpect(jsonPath("$.content[0].firstName", is(userList.get(0).getFirstName())))
                .andExpect(jsonPath("$.content[0].email", is(userList.get(0).getEmail())))
                .andExpect(jsonPath("$.content[0].password", is(userList.get(0).getPassword())));
    }

    @Test
    @DisplayName("Should return a user when id exists")
    @WithMockUser(roles = {"ADMIN", "USER"})
    void returnUserWhenIdExistsSuccessfully() throws JsonProcessingException, Exception {

        UUID uuid = this.admin.getUuid();
        // Given / Arrange
        when(service.findById(uuid)).thenReturn(this.admin);

        // When / Act
        ResultActions response = mockMvc.perform(get("/user/{uuid}", uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(admin)));

        // Then / Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid", is(admin.getUuid().toString())))
                .andExpect(jsonPath("$.firstName", is(admin.getFirstName())))
                .andExpect(jsonPath("$.email", is(admin.getEmail())))
                .andExpect(jsonPath("$.password", is(admin.getPassword())));
    }

    @Test
    @DisplayName("Should return a user when id not exists")
    @WithMockUser(roles = {"ADMIN", "USER"})
    void returnUserWhenIdNotExists() throws JsonProcessingException, Exception {

        UUID uuid = UUID.fromString("0dd57193-53f7-4f73-95f5-eb71215f155f");

        // Given / Arrange
        when(service.findById(uuid)).thenThrow(ResourceNotFoundException.class);

        // When / Act
        ResultActions response = mockMvc.perform(get("/user/{uuid}", uuid));

        // Then / Assert
        response.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Must insert a user successfully")
    @WithMockUser(roles = {"ADMIN"})
    void InsertUserSuccessfully() throws JsonProcessingException, Exception, UserException {

        User newUser = new User(null, "teste.admin", "teste.admin",     "teste.admin@gmail.com", passwordEncoder.encode("123456"), Set.of(roleUser, roleAdmin));

        // Given / Arrange
        when(service.insert(newUser)).thenReturn(newUser);

        // When / Act
        ResultActions response = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(newUser)));

        // Then / Assert
        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid", is(newUser.getUuid())))
                .andExpect(jsonPath("$.firstName", is(newUser.getFirstName())))
                .andExpect(jsonPath("$.email", is(newUser.getEmail())))
                .andExpect(jsonPath("$.password", is(newUser.getPassword())));
    }

    @Test
    @DisplayName("Must update a user successfully")
    @WithMockUser(roles = {"ADMIN"})
    void UpdateUserSuccessfully() throws JsonProcessingException, Exception, UserException {

        // Given / Arrange
        when(service.update(userUpdate)).thenReturn(userUpdate);

        // When / Act
        ResultActions response = mockMvc.perform(put("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(userUpdate)))
                .andDo(print());

        // Then / Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid", is(userUpdate.getUuid().toString())))
                .andExpect(jsonPath("$.firstName", is(userUpdate.getFirstName())))
                .andExpect(jsonPath("$.email", is(userUpdate.getEmail())))
                .andExpect(jsonPath("$.password", is(userUpdate.getPassword())));
    }

    @Test
    @DisplayName("Must not update a user successfully")
    @WithMockUser(roles = {"ADMIN"})
    void UpdateUserfail() throws JsonProcessingException, Exception, UserException {

        User aux = new User(UUID.fromString("dc368aa5-ed75-42a8-8055-70d9b7b134ea"), "teste.admin", "teste.admin",     "teste.admin@gmail.com", passwordEncoder.encode("123456"), Set.of(roleUser, roleAdmin));

        // Given / Arrange
        when(service.update(aux)).thenThrow(new ResourceNotFoundException("Resource not found!"));

        // When / Act
        ResultActions response = mockMvc.perform(put("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(aux)))
                .andDo(print());

        // Then / Assert
        response.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Must delete a user successfully")
    @WithMockUser(roles = {"ADMIN"})
    void deleteUserSucess() throws JsonProcessingException, Exception, UserException {

        UUID uuid = UUID.fromString("dc368aa5-ed75-42a8-8055-70d9b7b134ea");

        // Given / Arrange
        doNothing().when(service).delete(uuid);

        // When / Act
        ResultActions response = mockMvc.perform(delete("/user/{uuid}", uuid)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isNoContent());

        // Then / Assert
        verify(service, times(1)).delete(uuid);
    }

    @Test
    @DisplayName("Should return 404 Not Found when trying to delete a non-existent user")
    @WithMockUser(roles = {"ADMIN"})
    void deleteUserWhenNotFoundShouldReturn404() throws Exception {
        // Given / Arrange
        UUID uuidNotExist = UUID.randomUUID();

        // Mocka o método de serviço para lançar uma exceção de usuário não encontrado.
        doThrow(new ResourceNotFoundException(Messages.RESOURCE_NOT_FOUND)).when(service).delete(uuidNotExist);

        // When / Act
        mockMvc.perform(delete("/user/{uuid}", uuidNotExist)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 200 OK and a UserResponseDTO when login is successful")
    void shouldReturnOkAndUserResponseDTOWhenLoginIsSuccessful() throws Exception {

        String mockToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJteWFwcCIsInN1YiI6ImFkbWluQGdtYWlsLmNvbSIsImV4cCI6MTc1NzA4NTkxNX0.ur3ftZxkbywHmP2AWspami7ETR8usmbdmvEQqTEdjjM";
        UserRequestDTO requestDTO = new UserRequestDTO(this.admin.getEmail(), "123456");

        when(service.loadUserByUsername(requestDTO.email())).thenReturn(this.admin);
        when(passwordEncoder.matches(requestDTO.password(), this.admin.getPassword())).thenReturn(true);

        ResultActions response = mockMvc.perform(post("/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(requestDTO)));

        response.andExpect(status().isOk());

    }

    @Test
    @DisplayName("Should return 403 Forbidden when user does not exist")
    void shouldReturn403WhenUserDoesNotExist() throws Exception {
        UserRequestDTO requestDTO = new UserRequestDTO("inexistente@gmail.com", "123456");

        when(service.loadUserByUsername(requestDTO.email()))
                .thenThrow(new UsernameNotFoundException("User not found with email: " + requestDTO.email()));

        ResultActions response = mockMvc.perform(post("/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(requestDTO)));

        response.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when password is incorrect")
    void shouldReturn401WhenPasswordIsIncorrect() throws Exception {

        UserRequestDTO requestDTO = new UserRequestDTO(this.admin.getEmail(), "senha-incorreta");

        when(service.loadUserByUsername(requestDTO.email())).thenReturn(this.admin);

        when(passwordEncoder.matches(requestDTO.password(), this.admin.getPassword())).thenReturn(false);

        ResultActions response = mockMvc.perform(post("/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(requestDTO)));

        response.andExpect(status().isUnauthorized());
    }



    @AfterEach
    void cleanup() {
        userRepository.deleteAll(Arrays.asList(this.admin, this.user, this.userDelete, this.userUpdate));
        roleRepository.deleteAll(Arrays.asList(this.roleAdmin, this.roleUser));
    }



}
