package pl.dudios.shop.common.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.dudios.shop.common.model.Basket;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BasketRepo extends JpaRepository<Basket, Long> {

    List<Basket> findByCreatedLessThan(LocalDateTime minusDays);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Basket b where b.id = :id")
    Optional<Basket> findByIdForUpdate(Long id);

    @Query("DELETE FROM Basket b WHERE b.id in (:expiredBasketsIds)")
    @Modifying
    void deleteAllByIdIn(List<Long> expiredBasketsIds);

    @Query("delete from Basket b where b.id=:id")
    @Modifying
    void deleteBasketById(Long id);
}
