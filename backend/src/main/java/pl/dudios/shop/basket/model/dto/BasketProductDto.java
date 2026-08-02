package pl.dudios.shop.basket.model.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BasketProductDto(@NotNull Long productId, @NotNull @Positive Long quantity) {

}
