package br.com.bgrbarbosa.product_catalog.service.impl;

import br.com.bgrbarbosa.product_catalog.config.Messages;
import br.com.bgrbarbosa.product_catalog.model.User;
import br.com.bgrbarbosa.product_catalog.repository.UserRepository;
import br.com.bgrbarbosa.product_catalog.service.UserService;
import br.com.bgrbarbosa.product_catalog.service.exception.ResourceNotFoundException;
import br.com.bgrbarbosa.product_catalog.service.exception.UserException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;


    @Override
    public User loadUserByUsername(String email) throws UsernameNotFoundException {

        User aux = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return aux;
    }

    @Override
    public User insert(User user) throws UserException {
        User aux = user;
        if (repository.existsByEmail(aux.getEmail())) {
            throw new UserException(Messages.Existing_User);
        }
        aux.setPassword(passwordEncoder.encode(user.getPassword()));
        return repository.save(user);
    }

    @Override
    public List<User> findAll(Pageable page) {
        return repository.findAll();
    }

    @Override
    public User findById(UUID uuid) {
        return repository.findById(uuid).orElseThrow(
                () -> new ResourceNotFoundException(Messages.RESOURCE_NOT_FOUND + uuid)
        );
    }

    @Override
    public void delete(UUID uuid) {
        if (!repository.existsById(uuid)) {
            throw new ResourceNotFoundException("ID not found: " + uuid);
        }
        repository.deleteById(uuid);
    }

    @Override
    public User update(User user) {
        User aux = repository.findById(user.getUuid()).orElseThrow(
                () -> new ResourceNotFoundException("Resource not found!"));
        aux.setFirstName(user.getFirstName());
        aux.setLastName(user.getLastName());
        aux.setEmail(user.getEmail());
        aux.setPassword(passwordEncoder.encode(user.getPassword()));
        aux.setRoles(user.getRoles());
        return repository.save(aux);
    }

}
