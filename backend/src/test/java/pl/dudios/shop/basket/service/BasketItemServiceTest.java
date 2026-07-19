package pl.dudios.shop.basket.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dudios.shop.common.repository.BasketItemRepo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * Operacje na pojedynczej pozycji koszyka. Mockowane jest wyłącznie repozytorium
 * (granica DB) — serwis nie ma własnej logiki, więc testy pilnują tego, co widzi
 * wołający: że licznik dotyczy koszyka, o który zapytano, oraz że usunięcie jednej
 * pozycji kasuje dokładnie ją, a nie całą zawartość koszyka (repozytorium wystawia
 * obok siebie deleteById, deleteByBasketId i deleteAllByBasketId — pomyłka między
 * nimi wyczyściłaby klientowi cały koszyk).
 */
@ExtendWith(MockitoExtension.class)
class BasketItemServiceTest {

    @Mock
    private BasketItemRepo basketItemRepo;
    @InjectMocks
    private BasketItemService basketItemService;

    @Nested
    @DisplayName("counting the items of a basket")
    class CountingItems {

        @Test
        @DisplayName("reports the number of items held by the basket that was asked about")
        void reportsCountForRequestedBasket() {
            //Given
            given(basketItemRepo.countByBasketId(7L)).willReturn(3L);

            //When
            var count = basketItemService.countItemInBasket(7L);

            //Then
            assertThat(count).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("removing a single item from a basket")
    class RemovingItem {

        @Test
        @DisplayName("removes exactly the item the caller pointed at")
        void removesRequestedItem() {
            //Given
            var idCaptor = ArgumentCaptor.forClass(Long.class);

            //When
            basketItemService.deleteItemFromBasket(42L);

            //Then
            then(basketItemRepo).should().deleteById(idCaptor.capture());
            assertThat(idCaptor.getValue()).isEqualTo(42L);
        }

        @Test
        @DisplayName("never wipes the whole basket when only one item is being removed")
        void neverWipesWholeBasket() {
            //Given
            var itemId = 42L;

            //When
            basketItemService.deleteItemFromBasket(itemId);

            //Then
            then(basketItemRepo).should(never()).deleteByBasketId(any());
            then(basketItemRepo).should(never()).deleteAllByBasketId(any());
            then(basketItemRepo).should(never()).deleteAll();
        }
    }
}
