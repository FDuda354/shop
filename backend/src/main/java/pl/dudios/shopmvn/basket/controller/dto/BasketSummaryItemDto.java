package pl.dudios.shopmvn.basket.controller.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class BasketSummaryItemDto {

    private Long id;
    private Long quantity;
    private BigDecimal linePrice;

    //TODO: use ProductDto in common module
    private ProductItemDto product;
}
