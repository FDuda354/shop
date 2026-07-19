package pl.dudios.shop.basket.controller.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pl.dudios.shop.basket.controller.dto.BasketSummaryItemDto;
import pl.dudios.shop.common.model.Basket;
import pl.dudios.shop.common.model.BasketItem;
import pl.dudios.shop.common.model.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Reguły przeliczania koszyka na podsumowanie pokazywane klientowi.
 * BasketMapper to czysta funkcja bez granic systemu — nie ma tu żadnych mocków,
 * wszystkie encje (Basket, BasketItem, Product) są prawdziwymi obiektami domenowymi.
 * Testy pilnują tego, co widzi wołający: ceny linii, wartość brutto koszyka
 * i komplet danych produktu przeniesionych do DTO.
 */
class BasketMapperTest {

    @Nested
    @DisplayName("turning a basket into its summary")
    class BasketSummary {

        @Test
        @DisplayName("carries the basket id and every line over, in the order they sit in the basket")
        void carriesIdAndAllLinesInOrder() {
            //Given
            var basket = basketWith(
                    item(100L, product(1L, "Kubek", "19.99"), 2L),
                    item(200L, product(2L, "Talerz", "5.00"), 1L));

            //When
            var summary = BasketMapper.mapToBasketSummaryDto(basket);

            //Then
            assertThat(summary.id()).isEqualTo(7L);
            assertThat(summary.items())
                    .extracting(BasketSummaryItemDto::id, BasketSummaryItemDto::quantity)
                    .containsExactly(tuple(100L, 2L), tuple(200L, 1L));
        }

        @Test
        @DisplayName("returns a summary with no lines when the basket is empty")
        void returnsNoLinesForEmptyBasket() {
            //Given
            var basket = basketWith();

            //When
            var summary = BasketMapper.mapToBasketSummaryDto(basket);

            //Then
            assertThat(summary.items()).isEmpty();
        }
    }

    @Nested
    @DisplayName("pricing a single basket line")
    class LinePricing {

        @Test
        @DisplayName("charges the unit price once per ordered piece")
        void chargesUnitPricePerPiece() {
            //Given
            var basket = basketWith(item(100L, product(1L, "Kubek", "19.99"), 3L));

            //When
            var summary = BasketMapper.mapToBasketSummaryDto(basket);

            //Then
            assertThat(summary.items().getFirst().linePrice()).isEqualByComparingTo("59.97");
        }

        @Test
        @DisplayName("prices each line from its own product, so a cheap line does not bleed into an expensive one")
        void pricesEachLineFromItsOwnProduct() {
            //Given
            var basket = basketWith(
                    item(100L, product(1L, "Kubek", "19.99"), 2L),
                    item(200L, product(2L, "Talerz", "5.00"), 4L));

            //When
            var summary = BasketMapper.mapToBasketSummaryDto(basket);

            //Then
            assertThat(summary.items().getFirst().linePrice()).isEqualByComparingTo("39.98");
            assertThat(summary.items().get(1).linePrice()).isEqualByComparingTo("20.00");
        }
    }

    @Nested
    @DisplayName("gross value of the whole basket")
    class GrossValue {

        @Test
        @DisplayName("sums every line price into the amount the customer will pay")
        void sumsEveryLinePrice() {
            //Given
            var basket = basketWith(
                    item(100L, product(1L, "Kubek", "19.99"), 2L),
                    item(200L, product(2L, "Talerz", "5.00"), 1L));

            //When
            var summary = BasketMapper.mapToBasketSummaryDto(basket);

            //Then
            assertThat(summary.summary().grossValue()).isEqualByComparingTo("44.98");
        }

        @Test
        @DisplayName("charges zero for an empty basket instead of failing")
        void chargesZeroForEmptyBasket() {
            //Given
            var basket = basketWith();

            //When
            var summary = BasketMapper.mapToBasketSummaryDto(basket);

            //Then
            assertThat(summary.summary().grossValue()).isEqualByComparingTo("0");
        }
    }

    @Nested
    @DisplayName("product data attached to a line")
    class ProductSnapshot {

        @Test
        @DisplayName("copies the whole product presentation data so the client needs no second lookup")
        void copiesWholeProductPresentationData() {
            //Given
            var basket = basketWith(item(100L, product(1L, "Kubek", "19.99"), 1L));

            //When
            var summary = BasketMapper.mapToBasketSummaryDto(basket);

            //Then
            var mappedProduct = summary.items().getFirst().product();
            assertThat(mappedProduct.id()).isEqualTo(1L);
            assertThat(mappedProduct.name()).isEqualTo("Kubek");
            assertThat(mappedProduct.nameEn()).isEqualTo("Kubek EN");
            assertThat(mappedProduct.price()).isEqualByComparingTo("19.99");
            assertThat(mappedProduct.currency()).isEqualTo("PLN");
            assertThat(mappedProduct.image()).isEqualTo("Kubek.png");
            assertThat(mappedProduct.slug()).isEqualTo("kubek");
        }

        @Test
        @DisplayName("leaves the English name empty when the product has no translation, so the client can fall back to Polish")
        void leavesEnglishNameEmptyWithoutTranslation() {
            //Given
            var untranslated = Product.builder()
                    .id(1L)
                    .name("Kubek")
                    .price(new BigDecimal("19.99"))
                    .currency("PLN")
                    .build();
            var basket = basketWith(item(100L, untranslated, 1L));

            //When
            var summary = BasketMapper.mapToBasketSummaryDto(basket);

            //Then
            assertThat(summary.items().getFirst().product().nameEn()).isNull();
            assertThat(summary.items().getFirst().product().name()).isEqualTo("Kubek");
        }
    }

    private static Product product(Long id, String name, String price) {
        return Product.builder()
                .id(id)
                .name(name)
                .nameEn(name + " EN")
                .price(new BigDecimal(price))
                .currency("PLN")
                .image(name + ".png")
                .slug(name.toLowerCase())
                .build();
    }

    private static BasketItem item(Long id, Product product, Long quantity) {
        return BasketItem.builder()
                .id(id)
                .product(product)
                .quantity(quantity)
                .build();
    }

    private static Basket basketWith(BasketItem... items) {
        return Basket.builder()
                .id(7L)
                .created(LocalDateTime.now())
                .items(new ArrayList<>(List.of(items)))
                .build();
    }
}
