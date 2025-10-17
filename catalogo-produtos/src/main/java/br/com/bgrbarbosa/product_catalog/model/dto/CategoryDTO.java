package br.com.bgrbarbosa.product_catalog.model.dto;

import br.com.bgrbarbosa.product_catalog.config.Messages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record CategoryDTO(

        UUID uuidCategory,

        @NotBlank(message = Messages.NOT_BLANK)
        @Size(min = 3, max = 50, message = Messages.FIELD_SIZE_MESSAGE)
        String nameCategory,

        @NotBlank(message = Messages.NOT_BLANK)
        @Size(min = 3, max = 50, message = Messages.FIELD_SIZE_MESSAGE)
        String descCategory,

        LocalDate dtCreated,
        LocalDate dtUpdated) {

}
