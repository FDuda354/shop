package pl.dudios.shopmvn.basket.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.dudios.shopmvn.basket.service.BasketItemService;

@RestController
@AllArgsConstructor
@RequestMapping("/basketItems")
public class BasketItemController {

    private final BasketItemService basketItemService;

    @DeleteMapping("/{id}")
    public void deleteItemFromBasket(@PathVariable Long id) {
        basketItemService.deleteItemFromBasket(id);
    }

    @GetMapping("/counter/{basketId}")
    public Long countItemInBasket(@PathVariable Long basketId) {
        return basketItemService.countItemInBasket(basketId);
    }
}
