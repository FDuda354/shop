package pl.dudios.shopmvn.basket.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dudios.shopmvn.common.model.Basket;
import pl.dudios.shopmvn.common.repository.BasketItemRepo;
import pl.dudios.shopmvn.common.repository.BasketRepo;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class BasketCleanService {

    private final BasketRepo basketRepo;
    private final BasketItemRepo basketItemRepo;

    @Transactional
    @Scheduled(cron = "0 0 4 * * *")
    public void cleanOldBaskets() {
        List<Basket> baskets = basketRepo.findByCreatedLessThan(LocalDateTime.now().minusDays(2));
        List<Long> expiredBasketsIds = baskets.stream().map(Basket::getId).toList();
        log.info("Cleaning " + baskets.size() + " baskets");
        if (!expiredBasketsIds.isEmpty()) {
            basketItemRepo.deleteAllByBasketId(expiredBasketsIds);
            basketRepo.deleteAllByIdIn(expiredBasketsIds);
        }
    }
}
