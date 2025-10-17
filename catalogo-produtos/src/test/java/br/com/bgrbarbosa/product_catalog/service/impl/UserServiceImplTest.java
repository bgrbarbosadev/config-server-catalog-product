package br.com.bgrbarbosa.product_catalog.service.impl;

import br.com.bgrbarbosa.product_catalog.config.Messages;
import br.com.bgrbarbosa.product_catalog.model.Role;
import br.com.bgrbarbosa.product_catalog.model.User;
import br.com.bgrbarbosa.product_catalog.repository.UserRepository;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository repository;

    @Mock
    private BCryptPasswordEncoder bCrypt;

    @InjectMocks
    private UserServiceImpl service;

    private User user;
    private User userUpdate;
    private User admin;
    private User notExistUser;

    private Role roleAdmin;
    private Role roleUser;

    private List<User> listUser;

    UUID uuidUser = UUID.randomUUID();
    UUID uuidUserAdmin = UUID.randomUUID();
    UUID uuidNotExist = UUID.randomUUID();
    UUID uuidRoleUser = UUID.randomUUID();
    UUID uuidRoleAdmin = UUID.randomUUID();

    private String password;
    PageRequest pageable;

    @BeforeEach
    void setUp() {
        roleAdmin = new Role(uuidUserAdmin, "ROLE_ADMIN");
        roleUser = new Role(uuidRoleUser, "ROLE_USER");

        user = new User(uuidUser, "User", "User", "user@gmail.com", bCrypt.encode("123456"), Set.of(roleUser));
        userUpdate = new User(uuidUser, "User", "User", "user@gmail.com", bCrypt.encode("123456"), Set.of(roleUser));
        admin = new User(uuidUserAdmin, "Admin", "Admin", "admin@gmail.com", bCrypt.encode("123456"), Set.of(roleAdmin));
        notExistUser = new User(uuidNotExist, "Not Exist", "Not Exist", "notexist@gmail.com", bCrypt.encode("123456"), Set.of(roleAdmin));;

        listUser = List.of(user, admin);

        pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "uuid");
    }

    @Test
    @DisplayName("Must return a user when username exists")
    void loadUserByUsernameWhenUserNameExists() {

        // Given / Arrange
        Mockito.when(repository.findByEmail(user.getEmail())).thenReturn(Optional.ofNullable(user));

        // When / Act
        User result = service.loadUserByUsername(user.getEmail());

       // Then / Assert
        assertEquals(result.getClass(), User.class);
        assertEquals(result.getClass(), user.getClass());
        assertEquals(result.getUuid(), user.getUuid());
        assertEquals(result.getEmail(), user.getEmail());
        assertEquals(result.getPassword(), user.getPassword());
        assertEquals(result.getRoles(), user.getRoles());
        assertEquals(result.getFirstName(), user.getFirstName());
        assertEquals(result.getLastName(), user.getLastName());
    }

    @Test
    @DisplayName("Must not return a user if the username does not exist")
    void loadUserByUsernameWhenUserNameNotExists() throws UserException {

        //Given / Arrange
        Mockito.when(repository.findByEmail(notExistUser.getEmail()))
                .thenThrow(new UsernameNotFoundException(Messages.RESOURCE_NOT_FOUND));

        // When / Act
        assertThrows(UsernameNotFoundException.class, () -> {
            service.loadUserByUsername(notExistUser.getEmail());
        }, Messages.RESOURCE_NOT_FOUND);

    }

    @Test
    @DisplayName("You must insert a user successfully")
    void insertUserSuccessfully() throws UserException {

        //Given / Arrange
        Mockito.when(repository.save(user)).thenReturn(user);

        // When / Act
        User result = service.insert(user);

        // Then / Assert
        verify(repository, times(1)).save(user);
        assertEquals(result.getClass(), user.getClass());
        assertEquals(result.getUuid(), user.getUuid());
        assertEquals(result.getEmail(), user.getEmail());
        assertEquals(result.getPassword(), user.getPassword());
        assertEquals(result.getRoles(), user.getRoles());
        assertEquals(result.getFirstName(), user.getFirstName());
        assertEquals(result.getLastName(), user.getLastName());

    }

    @Test
    @DisplayName("Do not insert a user if the email already exists.")
    void insertUserNotSuccessfully() throws UserException {

        //Given / Arrange
        Mockito.when(repository.existsByEmail(user.getEmail())).thenReturn(true);

        // When / Act
        assertThrows(UserException.class, () -> {
            service.insert(user);
        }, Messages.Existing_User);

    }

    @Test
    @DisplayName("Must return a list of users")
    void ReturnsAListOfUsers() {

        // Given / Arrange
        Mockito.when(repository.findAll()).thenReturn(listUser);

        // When / Act
        List<User> result = service.findAll(pageable);

        // Then / Assert
        Assertions.assertEquals(result.size(), 2);
        Assertions.assertEquals(result.get(0).getClass(), User.class);
    }

    @Test
    @DisplayName("Must return a user when id exists")
    void findByIdWhenIdExist() {

        // Given / Arrange
        Mockito.when(repository.findById(any())).thenReturn(Optional.ofNullable(user));

        // When / Act
        User result = service.findById(any());

        // Then / Assert
        assertEquals(result.getClass(), User.class);
        assertEquals(result.getClass(), user.getClass());
        assertEquals(result.getUuid(), user.getUuid());
        assertEquals(result.getEmail(), user.getEmail());
        assertEquals(result.getPassword(), user.getPassword());
        assertEquals(result.getRoles(), user.getRoles());
        assertEquals(result.getFirstName(), user.getFirstName());
        assertEquals(result.getLastName(), user.getLastName());

    }

    @Test
    @DisplayName("Must not return a category if the id does not exist")
    void findByIdWhenIdNotExists() throws UserException {

        //Given / Arrange
        when(repository.findById(uuidNotExist))
                .thenReturn(Optional.empty());

        // When / Act
        assertThrows(ResourceNotFoundException.class, () -> {
            service.findById(uuidNotExist);
        }, Messages.RESOURCE_NOT_FOUND);

    }

    @Test
    @DisplayName("Should delete user when id exists")
    void deleteByIdWhenIdExists() throws UserException {

        // Given / Arrange
        when(repository.existsById(uuidUser)).thenReturn(true);

        // When / Act
        service.delete(uuidUser);

        // Then / Assert
        verify(repository, times(1)).deleteById(uuidUser);

    }

    @Test
    @DisplayName("Must not return a user if the id does not exist")
    void deleteByIdWhenIdNotExists() throws UserException {

        // When / Act
        assertThrows(ResourceNotFoundException.class, () -> {
            service.delete(any());
        }, Messages.RESOURCE_NOT_FOUND);

    }

    @Test
    void whenUpdateUser_thenReturnsUpdatedUser() {
        // Given
        when(repository.findById(user.getUuid())).thenReturn(Optional.of(user));
        when(repository.save(any(User.class))).thenReturn(userUpdate);

        // When
        User result = service.update(user);

        // Then
        assertNotNull(result);
        assertEquals(userUpdate.getFirstName(), result.getFirstName());
        assertEquals(userUpdate.getLastName(), result.getLastName());
        assertEquals(userUpdate.getEmail(), result.getEmail());
        assertEquals(userUpdate.getPassword(), user.getPassword());

        verify(repository, times(1)).findById(user.getUuid());
        verify(repository, times(1)).save(user);
    }

    @Test
    void whenUpdateUserNotFound_thenThrowsResourceNotFoundException() {
        // Given
        when(repository.findById(user.getUuid())).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> service.update(user));

        verify(repository, times(1)).findById(user.getUuid());
        verify(repository, never()).save(any(User.class));
    }
}