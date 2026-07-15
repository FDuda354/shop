package pl.dudios.shopmvn.basket.controller.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ProductItemDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private String currency;
    private String image;
    private String slug;
}
