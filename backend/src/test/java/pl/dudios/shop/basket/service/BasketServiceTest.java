package pl.dudios.shop.basket.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dudios.shop.basket.model.dto.BasketProductDto;
import pl.dudios.shop.common.model.Basket;
import pl.dudios.shop.common.model.BasketItem;
import pl.dudios.shop.common.model.Product;
import pl.dudios.shop.common.repository.BasketRepo;
import pl.dudios.shop.common.repository.ProductRepo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * Reguły dokładania produktów do koszyka. Mockowane są wyłącznie repozytoria
 * (granica DB); encje Basket/BasketItem/Product to prawdziwe obiekty domenowe,
 * więc testy pilnują obserwowalnego wyniku — zawartości koszyka zwróconej
 * wołającemu — a nie wewnętrznych kroków serwisu.
 */
@ExtendWith(MockitoExtension.class)
class BasketServiceTest {

    @Mock
    private BasketRepo basketRepo;
    @Mock
    private ProductRepo productRepo;
    @InjectMocks
    private BasketService basketService;

    @Nested
    @DisplayName("adding a product when no basket exists yet")
    class AddingToNewBasket {

        @Test
        @DisplayName("creates a fresh basket and puts the product inside it")
        void createsFreshBasketWithProduct() {
            //Given
            given(productRepo.findById(1L)).willReturn(Optional.of(product(1L)));
            given(basketRepo.save(any())).willReturn(emptyBasket(10L));

            //When
            var basket = basketService.addProductToBasket(0L, new BasketProductDto(1L, 2L));

            //Then
            assertThat(basket.getId()).isEqualTo(10L);
            assertThat(basket.getItems()).hasSize(1);
            assertThat(basket.getItems().getFirst().getProduct().getId()).isEqualTo(1L);
            assertThat(basket.getItems().getFirst().getQuantity()).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("adding a product to an existing basket")
    class AddingToExistingBasket {

        @Test
        @DisplayName("appends the product without creating a new basket")
        void appendsProductWithoutNewBasket() {
            //Given
            given(basketRepo.findById(5L)).willReturn(Optional.of(emptyBasket(5L)));
            given(productRepo.findById(1L)).willReturn(Optional.of(product(1L)));

            //When
            var basket = basketService.addProductToBasket(5L, new BasketProductDto(1L, 1L));

            //Then
            assertThat(basket.getId()).isEqualTo(5L);
            assertThat(basket.getItems()).hasSize(1);
            then(basketRepo).should(never()).save(any());
        }

        @Test
        @DisplayName("adds the requested quantity to a product already in the basket instead of duplicating it")
        void addsRequestedQuantityForDuplicateProduct() {
            //Given
            var basket = emptyBasket(5L);
            basket.addProduct(BasketItem.builder().product(product(1L)).quantity(1L).build());
            given(basketRepo.findById(5L)).willReturn(Optional.of(basket));
            given(productRepo.findById(1L)).willReturn(Optional.of(product(1L)));

            //When
            var result = basketService.addProductToBasket(5L, new BasketProductDto(1L, 5L));

            //Then
            assertThat(result.getItems()).hasSize(1);
            assertThat(result.getItems().getFirst().getQuantity()).isEqualTo(6L);
        }

        @Test
        @DisplayName("refuses to add an unknown product and leaves the basket untouched")
        void refusesUnknownProduct() {
            //Given
            var basket = emptyBasket(5L);
            given(basketRepo.findById(5L)).willReturn(Optional.of(basket));
            given(productRepo.findById(99L)).willReturn(Optional.empty());

            //When //Then
            assertThatThrownBy(() -> basketService.addProductToBasket(5L, new BasketProductDto(99L, 1L)))
                    .isInstanceOf(NoSuchElementException.class);
            assertThat(basket.getItems()).isEmpty();
        }
    }

    @Nested
    @DisplayName("updating basket quantities")
    class UpdatingBasket {

        @Test
        @DisplayName("overwrites quantities only for products present in the request")
        void overwritesQuantitiesForRequestedProducts() {
            //Given
            var basket = emptyBasket(5L);
            basket.addProduct(BasketItem.builder().product(product(1L)).quantity(1L).build());
            basket.addProduct(BasketItem.builder().product(product(2L)).quantity(3L).build());
            given(basketRepo.findById(5L)).willReturn(Optional.of(basket));

            //When
            var result = basketService.updateBasket(5L, List.of(new BasketProductDto(1L, 7L)));

            //Then
            assertThat(result.getItems())
                    .extracting(item -> item.getProduct().getId(), BasketItem::getQuantity)
                    .containsExactlyInAnyOrder(
                            org.assertj.core.groups.Tuple.tuple(1L, 7L),
                            org.assertj.core.groups.Tuple.tuple(2L, 3L));
        }
    }

    private static Product product(Long id) {
        return Product.builder().id(id).build();
    }

    private static Basket emptyBasket(Long id) {
        return Basket.builder()
                .id(id)
                .created(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();
    }
}
