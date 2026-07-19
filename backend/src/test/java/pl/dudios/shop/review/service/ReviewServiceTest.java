package pl.dudios.shop.review.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dudios.shop.common.model.Review;
import pl.dudios.shop.review.repository.ReviewRepo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * Obsługa opinii o produktach. Mockowane jest wyłącznie repozytorium (granica bazy danych);
 * encja Review to prawdziwy obiekt domenowy. Serwis jest cienką warstwą nad repozytorium,
 * więc testy pilnują tego, co widzi wołający (kontroler /review i /reviews): że zapisana
 * opinia wraca z nadanym id, że trafia do bazy z kompletem danych potrzebnych do jej
 * wyświetlenia, że użytkownik dostaje wyłącznie swoje opinie — nigdy cudze ani całej
 * tabeli — oraz że usuwanie dotyka dokładnie jednej wskazanej opinii.
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepo reviewRepo;
    @InjectMocks
    private ReviewService reviewService;

    @Nested
    @DisplayName("adding a review")
    class AddingReview {

        @Test
        @DisplayName("hands back the review with the id assigned on save, so the caller can address it later")
        void returnsPersistedReviewWithAssignedId() {
            //Given
            var submitted = newReview(null, 42L, 7L, "Filip", "Świetny laptop");
            given(reviewRepo.save(any())).willReturn(newReview(100L, 42L, 7L, "Filip", "Świetny laptop"));

            //When
            var saved = reviewService.addReview(submitted);

            //Then
            assertThat(saved.getId()).isEqualTo(100L);
            assertThat(saved.getContent()).isEqualTo("Świetny laptop");
        }

        @Test
        @DisplayName("stores the review together with its author and the product it belongs to")
        void storesReviewWithAuthorAndProduct() {
            //Given
            var submitted = newReview(null, 42L, 7L, "Filip", "Świetny laptop");

            //When
            reviewService.addReview(submitted);

            //Then
            var captor = ArgumentCaptor.forClass(Review.class);
            then(reviewRepo).should().save(captor.capture());
            assertThat(captor.getValue())
                    .extracting(Review::getProductId, Review::getUserId, Review::getAuthorName, Review::getContent)
                    .containsExactly(42L, 7L, "Filip", "Świetny laptop");
        }
    }

    @Nested
    @DisplayName("listing the reviews of a single user")
    class ListingUserReviews {

        @Test
        @DisplayName("returns every review written by the asking user, with the product each one describes")
        void returnsReviewsOfAskingUser() {
            //Given
            given(reviewRepo.findALLByUserId(7L)).willReturn(List.of(
                    newReview(1L, 42L, 7L, "Filip", "Świetny laptop"),
                    newReview(2L, 43L, 7L, "Filip", "Mysz też dobra")));

            //When
            var reviews = reviewService.getUserReviews(7L);

            //Then
            assertThat(reviews)
                    .extracting(Review::getId, Review::getProductId)
                    .containsExactly(
                            org.assertj.core.groups.Tuple.tuple(1L, 42L),
                            org.assertj.core.groups.Tuple.tuple(2L, 43L));
            assertThat(reviews).allSatisfy(review -> assertThat(review.getUserId()).isEqualTo(7L));
        }

        @Test
        @DisplayName("returns an empty list — not null — for a user who has not reviewed anything yet")
        void returnsEmptyListForUserWithoutReviews() {
            //Given
            given(reviewRepo.findALLByUserId(7L)).willReturn(List.of());

            //When
            var reviews = reviewService.getUserReviews(7L);

            //Then
            assertThat(reviews).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("shows nothing to an anonymous caller instead of falling back to everybody's reviews")
        void showsNothingToAnonymousCaller() {
            //Given
            given(reviewRepo.findALLByUserId(null)).willReturn(List.of());

            //When
            var reviews = reviewService.getUserReviews(null);

            //Then
            assertThat(reviews).isEmpty();
            then(reviewRepo).should(never()).findAll();
        }
    }

    @Nested
    @DisplayName("deleting a review")
    class DeletingReview {

        @Test
        @DisplayName("removes exactly the review the caller pointed at and nothing else")
        void removesOnlyTheIndicatedReview() {
            //When
            reviewService.deleteReview(7L);

            //Then
            then(reviewRepo).should().deleteById(7L);
            then(reviewRepo).shouldHaveNoMoreInteractions();
        }
    }

    private static Review newReview(Long id, Long productId, Long userId, String authorName, String content) {
        return Review.builder()
                .id(id)
                .productId(productId)
                .userId(userId)
                .authorName(authorName)
                .content(content)
                .build();
    }
}
