package pl.dudios.shop.basket.controller.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record BasketSummaryDto(
        Long id,
        List<BasketSummaryItemDto> items,
        SummaryDto summary
) {
}
