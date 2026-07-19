package pl.dudios.shop.basket.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dudios.shop.common.model.Basket;
import pl.dudios.shop.common.repository.BasketItemRepo;
import pl.dudios.shop.common.repository.BasketRepo;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

/**
 * Nocne sprzątanie porzuconych koszyków. Mockowane są wyłącznie repozytoria
 * (granica DB) — encja Basket to prawdziwy obiekt domenowy. Testy pilnują reguł
 * biznesowych widocznych po stronie bazy: który koszyk kwalifikuje się do
 * usunięcia (starszy niż dwa dni), że kasowane są dokładnie te koszyki, które
 * zwróciło repozytorium, że pozycje znikają przed koszykami (klucz obcy) oraz
 * że przy braku przeterminowanych koszyków nie leci żaden DELETE.
 */
@ExtendWith(MockitoExtension.class)
class BasketCleanServiceTest {

    @Mock
    private BasketRepo basketRepo;
    @Mock
    private BasketItemRepo basketItemRepo;
    @InjectMocks
    private BasketCleanService basketCleanService;

    @Nested
    @DisplayName("removing baskets that have expired")
    class CleaningExpiredBaskets {

        @Test
        @DisplayName("deletes every expired basket, addressing them by their ids")
        void deletesEveryExpiredBasket() {
            //Given
            given(basketRepo.findByCreatedLessThan(any()))
                    .willReturn(List.of(expiredBasket(7L), expiredBasket(8L), expiredBasket(9L)));

            //When
            basketCleanService.cleanOldBaskets();

            //Then
            var idsCaptor = ArgumentCaptor.<List<Long>>captor();
            then(basketRepo).should().deleteAllByIdIn(idsCaptor.capture());
            assertThat(idsCaptor.getValue()).containsExactly(7L, 8L, 9L);
        }

        @Test
        @DisplayName("wipes the items of the expired baskets before the baskets themselves")
        void wipesItemsBeforeBaskets() {
            //Given
            given(basketRepo.findByCreatedLessThan(any())).willReturn(List.of(expiredBasket(7L)));

            //When
            basketCleanService.cleanOldBaskets();

            //Then
            // Kolejność jest tu regułą, a nie szczegółem: basket_items trzyma FK na baskets,
            // więc skasowanie koszyka przed jego pozycjami wywala integralność bazy.
            var order = inOrder(basketItemRepo, basketRepo);
            then(basketItemRepo).should(order).deleteAllByBasketId(List.of(7L));
            then(basketRepo).should(order).deleteAllByIdIn(List.of(7L));
        }

        @Test
        @DisplayName("removes items of exactly the baskets being deleted, so no orphaned items are left behind")
        void removesItemsOfExactlyTheDeletedBaskets() {
            //Given
            given(basketRepo.findByCreatedLessThan(any()))
                    .willReturn(List.of(expiredBasket(3L), expiredBasket(4L)));

            //When
            basketCleanService.cleanOldBaskets();

            //Then
            var itemIdsCaptor = ArgumentCaptor.<List<Long>>captor();
            var basketIdsCaptor = ArgumentCaptor.<List<Long>>captor();
            then(basketItemRepo).should().deleteAllByBasketId(itemIdsCaptor.capture());
            then(basketRepo).should().deleteAllByIdIn(basketIdsCaptor.capture());
            assertThat(itemIdsCaptor.getValue()).isEqualTo(basketIdsCaptor.getValue());
        }

        @Test
        @DisplayName("treats a basket as expired only once it is older than two days")
        void treatsBasketAsExpiredAfterTwoDays() {
            //Given
            given(basketRepo.findByCreatedLessThan(any())).willReturn(List.of());

            //When
            basketCleanService.cleanOldBaskets();

            //Then
            var cutoffCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
            then(basketRepo).should().findByCreatedLessThan(cutoffCaptor.capture());
            assertThat(cutoffCaptor.getValue())
                    .isCloseTo(LocalDateTime.now().minusDays(2), within(10, ChronoUnit.SECONDS));
        }
    }

    @Nested
    @DisplayName("running the cleanup when nothing has expired")
    class NothingToClean {

        @Test
        @DisplayName("issues no delete at all when every basket is still fresh")
        void issuesNoDeleteWhenNothingExpired() {
            //Given
            given(basketRepo.findByCreatedLessThan(any())).willReturn(List.of());

            //When
            basketCleanService.cleanOldBaskets();

            //Then
            then(basketItemRepo).shouldHaveNoInteractions();
            then(basketRepo).should(never()).deleteAllByIdIn(any());
        }
    }

    private static Basket expiredBasket(Long id) {
        return Basket.builder()
                .id(id)
                .created(LocalDateTime.now().minusDays(3))
                .build();
    }
}
