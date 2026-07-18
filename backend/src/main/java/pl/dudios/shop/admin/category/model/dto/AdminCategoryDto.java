package pl.dudios.shop.admin.category.model.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record AdminCategoryDto(
        @NotBlank @Length(min = 3, max = 255) String name,
        String description,
        // Wersja angielska — opcjonalna.
        @Length(max = 255) String nameEn,
        String descriptionEn,
        @NotBlank @Length(min = 3, max = 255) String slug
) {
}
