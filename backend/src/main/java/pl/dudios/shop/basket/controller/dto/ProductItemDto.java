package pl.dudios.shop.basket.controller.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ProductItemDto {
    private Long id;
    private String name;
    private String nameEn;
    private BigDecimal price;
    private String currency;
    private String image;
    private String slug;
}
