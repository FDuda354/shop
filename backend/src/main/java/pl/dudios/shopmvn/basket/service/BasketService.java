package pl.dudios.shopmvn.basket.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dudios.shopmvn.basket.model.dto.BasketProductDto;
import pl.dudios.shopmvn.common.model.Basket;
import pl.dudios.shopmvn.common.model.BasketItem;
import pl.dudios.shopmvn.common.model.Product;
import pl.dudios.shopmvn.common.repository.BasketRepo;
import pl.dudios.shopmvn.common.repository.ProductRepo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class BasketService {

    private final BasketRepo basketRepo;
    private final ProductRepo productRepo;

    //TODO: Upgrade this 1
    public Basket getBasket(Long id) {
        return basketRepo.findById(id).orElse(Basket.builder().created(LocalDateTime.now()).items(new ArrayList<>()).build());
    }

    @Transactional
    public Basket addProductToBasket(Long id, BasketProductDto basketProductDto) {
        Basket basket = getInitializedBasket(id);

        return basket.addProduct(BasketItem.builder()
                .product(getProduct(basketProductDto.productId()))
                .quantity(basketProductDto.quantity())
                .basketId(basket.getId())
                .build());
    }

    //TODO: Upgrade this 1
    private Basket getInitializedBasket(Long id) {
        if (id == null || id <= 0) {
            return basketRepo.save(Basket.builder()
                    .created(LocalDateTime.now())
                    .build());
        }
        try {
            return basketRepo.findById(id).orElseThrow();
        } catch (Exception e) {
            return basketRepo.save(Basket.builder()
                    .created(LocalDateTime.now())
                    .items(new ArrayList<>())
                    .build());
        }
    }

    private Product getProduct(Long productId) {
        return productRepo.findById(productId).orElseThrow();
    }

    @Transactional
    public Basket updateBasket(Long id, List<BasketProductDto> basketProductDtos) {
        Basket basket = basketRepo.findById(id).orElseThrow();
        basket.getItems().forEach(basketItem -> {
            basketProductDtos.stream()
                    .filter(basketProductDto -> basketProductDto.productId().equals(basketItem.getProduct().getId()))
                    .findFirst()
                    .ifPresent(basketProductDto -> basketItem.setQuantity(basketProductDto.quantity()));

        });
        return basket;
    }

}
