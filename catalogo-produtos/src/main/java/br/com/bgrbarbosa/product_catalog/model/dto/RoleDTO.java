package br.com.bgrbarbosa.product_catalog.model.dto;

import java.util.UUID;

public record RoleDTO(
        UUID uuid,
        String authority
) { }
