package pl.dudios.shopmvn.review.controller;

import lombok.AllArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.dudios.shopmvn.common.model.Review;
import pl.dudios.shopmvn.review.service.ReviewService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@AllArgsConstructor
@Validated
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/review")
    public Review addReview(@RequestBody @Valid ReviewDto review, @AuthenticationPrincipal Long userId) {
        System.out.println(userId);
        return reviewService.addReview(
                Review.builder()
                        .authorName(cleanContent(review.authorName()))
                        .content(cleanContent(review.content()))
                        .productId(review.productId())
                        .userId(userId)
                        .build()
        );
    }

    @GetMapping("/reviews")
    public List<Review> getUserReviews(@AuthenticationPrincipal Long userId) {
        return reviewService.getUserReviews(userId);
    }

    @DeleteMapping("/review/{id}")
    public void deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
    }

    private String cleanContent(String text) {
        return Jsoup.clean(text, Safelist.none());
    }
}
