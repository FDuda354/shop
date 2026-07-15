package pl.dudios.shopmvn.basket.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.dudios.shopmvn.basket.controller.dto.BasketSummaryDto;
import pl.dudios.shopmvn.basket.controller.mapper.BasketMapper;
import pl.dudios.shopmvn.basket.model.dto.BasketProductDto;
import pl.dudios.shopmvn.basket.service.BasketService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/basket")
public class BasketContorller {

    private final BasketService basketService;

    @GetMapping("/{id}")
    public BasketSummaryDto getBasket(@PathVariable Long id) {
        return BasketMapper.mapToBasketSummaryDto(basketService.getBasket(id));
    }

    @PutMapping("/{id}")
    public BasketSummaryDto addProductToBasket(@PathVariable Long id, @RequestBody BasketProductDto basketProductDto) {
        return BasketMapper.mapToBasketSummaryDto(basketService.addProductToBasket(id, basketProductDto));
    }

    @PutMapping("/{id}/update")
    public BasketSummaryDto updateBasket(@PathVariable Long id, @RequestBody List<BasketProductDto> basketProductDtos) {
        return BasketMapper.mapToBasketSummaryDto(basketService.updateBasket(id, basketProductDtos));
    }

}
