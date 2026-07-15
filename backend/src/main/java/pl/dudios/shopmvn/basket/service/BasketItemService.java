package pl.dudios.shopmvn.basket.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.dudios.shopmvn.common.repository.BasketItemRepo;

@Service
@AllArgsConstructor
public class BasketItemService {

    private final BasketItemRepo basketItemRepo;

    public void deleteItemFromBasket(Long id) {
        basketItemRepo.deleteById(id);
    }

    public Long countItemInBasket(Long basketId) {
        return basketItemRepo.countByBasketId(basketId);
    }
}
