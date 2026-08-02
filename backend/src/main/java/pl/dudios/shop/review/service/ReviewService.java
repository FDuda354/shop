package pl.dudios.shop.review.service;

import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import pl.dudios.shop.common.model.Review;
import pl.dudios.shop.review.repository.ReviewRepo;

import java.util.List;

@Service
@AllArgsConstructor
public class ReviewService {

    private final ReviewRepo reviewRepo;

    public Review addReview(Review review) {
        return reviewRepo.save(review);
    }

    public List<Review> getUserReviews(Long userId) {
        return reviewRepo.findALLByUserId(userId);
    }

    public void deleteReview(Long id, Long userId) {
        Review review = reviewRepo.findById(id).orElseThrow();
        if (!userId.equals(review.getUserId())) {
            throw new AccessDeniedException("Only the review author can delete it");
        }
        reviewRepo.delete(review);
    }
}
