package br.com.bgrbarbosa.product_catalog.model.dto;

import br.com.bgrbarbosa.product_catalog.model.Role;
import java.util.Set;
import java.util.UUID;

public record UserDTO(
        UUID uuid,
        String firstName,
        String lastName,
        String email,
        String password,
        Set<Role> roles
) { }
