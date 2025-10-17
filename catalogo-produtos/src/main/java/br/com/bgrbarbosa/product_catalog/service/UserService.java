package br.com.bgrbarbosa.product_catalog.service;

import br.com.bgrbarbosa.product_catalog.model.Category;
import br.com.bgrbarbosa.product_catalog.model.Role;
import br.com.bgrbarbosa.product_catalog.model.User;
import br.com.bgrbarbosa.product_catalog.service.exception.UserException;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.UUID;

public interface UserService {

    User insert(User user) throws UserException;

    List<User> findAll(Pageable page);

    User findById(UUID uuid);

    void delete(UUID uuid);

    User update(User user);

    User loadUserByUsername(String email) throws UsernameNotFoundException;
}
