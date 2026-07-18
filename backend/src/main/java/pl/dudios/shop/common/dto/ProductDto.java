package pl.dudios.shop.common.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProductDto(
        Long id,
        String name,
        String description,
        String nameEn,
        String descriptionEn,
        BigDecimal price,
        String currency,
        String image,
        String slug
) {
}
