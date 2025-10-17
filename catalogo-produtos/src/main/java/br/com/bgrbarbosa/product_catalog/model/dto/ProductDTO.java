package br.com.bgrbarbosa.product_catalog.model.dto;


import br.com.bgrbarbosa.product_catalog.config.Messages;
import br.com.bgrbarbosa.product_catalog.model.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record ProductDTO(
        UUID uuidProduct,

        @NotBlank(message = Messages.NOT_BLANK)
        @Size(min = 3, max = 50, message = Messages.FIELD_SIZE_MESSAGE)
        String nameProduct,

        @NotBlank(message = Messages.NOT_BLANK)
        @Size(min = 5, max = 150, message = Messages.FIELD_SIZE_MESSAGE)
        String descriptionProduct,

        @NotNull(message = Messages.NOT_NULL)
        Double priceProduct,

        @NotBlank(message = Messages.NOT_BLANK)
        @Size(min = 5, max = 50, message = Messages.FIELD_SIZE_MESSAGE)
        String urlProduct,

        LocalDate dtCreated,
        LocalDate dtUpdated,

        @NotNull(message = Messages.NOT_NULL)
        Category categoryProduct
) {
}
