package pl.dudios.shopmvn.basket.controller.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BasketSummaryDto {

    private Long id;
    private List<BasketSummaryItemDto> items;

    //TODO: Zamienic na BigDecimal
    private SummaryDto summary;
}
