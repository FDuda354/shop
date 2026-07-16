package pl.dudios.shop.basket.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.dudios.shop.common.repository.BasketItemRepo;

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
