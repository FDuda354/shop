package pl.dudios.shop.basket.controller.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record BasketSummaryItemDto(
        Long id,
        Long quantity,
        BigDecimal linePrice,
        ProductItemDto product
) {
}
