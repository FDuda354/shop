package pl.dudios.shop.basket.controller.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class SummaryDto {
    private BigDecimal grossValue;
}
