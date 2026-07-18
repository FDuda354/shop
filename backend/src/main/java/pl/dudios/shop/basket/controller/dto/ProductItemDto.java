package pl.dudios.shop.basket.controller.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProductItemDto(
        Long id,
        String name,
        String nameEn,
        BigDecimal price,
        String currency,
        String image,
        String slug
) {
}
