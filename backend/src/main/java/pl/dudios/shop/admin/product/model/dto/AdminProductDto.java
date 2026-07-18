package pl.dudios.shop.admin.product.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import pl.dudios.shop.admin.product.model.AdminProductCurrency;

import java.math.BigDecimal;

public record AdminProductDto(
        @NotBlank @Length(min = 3, max = 255) String name,
        @NotBlank @Length(min = 3, max = 100) String description,
        String fullDescription,
        // Wersja angielska — opcjonalna (bez @NotBlank), limity jak dla polskiej.
        @Length(max = 255) String nameEn,
        @Length(max = 100) String descriptionEn,
        String fullDescriptionEn,
        @NotNull Long categoryId,
        @NotNull @Min(0) BigDecimal price,
        AdminProductCurrency currency,
        String image,
        @NotBlank @Length(min = 3, max = 255) String slug
) {
}
