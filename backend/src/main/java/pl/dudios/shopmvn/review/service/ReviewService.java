package pl.dudios.shopmvn.review.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.dudios.shopmvn.common.model.Review;
import pl.dudios.shopmvn.review.repository.ReviewRepo;

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

    public void deleteReview(Long id) {
        reviewRepo.deleteById(id);
    }
}
