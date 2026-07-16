package pl.dudios.shop.basket.controller.mapper;

import pl.dudios.shop.basket.controller.dto.BasketSummaryDto;
import pl.dudios.shop.basket.controller.dto.BasketSummaryItemDto;
import pl.dudios.shop.basket.controller.dto.ProductItemDto;
import pl.dudios.shop.basket.controller.dto.SummaryDto;
import pl.dudios.shop.common.model.Basket;
import pl.dudios.shop.common.model.BasketItem;
import pl.dudios.shop.common.model.Product;

import java.math.BigDecimal;
import java.util.List;

public class BasketMapper {

    private BasketMapper() {
    }

    public static BasketSummaryDto mapToBasketSummaryDto(Basket basket) {
        return BasketSummaryDto.builder()
                .id(basket.getId())
                .items(mapBasketItems(basket.getItems()))
                .summary(mapToSummary(basket.getItems()))
                .build();
    }

    private static List<BasketSummaryItemDto> mapBasketItems(List<BasketItem> items) {
        return items.stream()
                .map(BasketMapper::mapToBasketItem)
                .toList();
    }

    private static BasketSummaryItemDto mapToBasketItem(BasketItem item) {
        return BasketSummaryItemDto.builder()
                .id(item.getId())
                .quantity(item.getQuantity())
                .product(mapToProductItemDto(item.getProduct()))
                .linePrice(calculateLinePrice(item))
                .build();
    }

    private static ProductItemDto mapToProductItemDto(Product product) {
        return ProductItemDto.builder()
                .id(product.getId())
                .name(product.getName())
                .nameEn(product.getNameEn())
                .currency(product.getCurrency())
                .image(product.getImage())
                .price(product.getPrice())
                .slug(product.getSlug())
                .build();
    }

    private static BigDecimal calculateLinePrice(BasketItem item) {
        return item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    private static SummaryDto mapToSummary(List<BasketItem> items) {
        return SummaryDto.builder()
                .grossValue(calculateTotalPrice(items))
                .build();
    }

    private static BigDecimal calculateTotalPrice(List<BasketItem> items) {
        return items.stream()
                .map(BasketMapper::calculateLinePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

    }

}
