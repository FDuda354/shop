package pl.dudios.shop.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dudios.shop.common.model.Review;

import java.util.List;

@Repository
public interface ReviewRepo extends JpaRepository<Review, Long> {


    List<Review> findALLByUserId(Long userId);
}
