package pl.dudios.shopmvn.admin.product.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dudios.shopmvn.common.model.Basket;
import pl.dudios.shopmvn.basket.model.dto.BasketProductDto;
import pl.dudios.shopmvn.common.repository.BasketRepo;
import pl.dudios.shopmvn.basket.service.BasketService;
import pl.dudios.shopmvn.common.model.Product;
import pl.dudios.shopmvn.common.repository.ProductRepo;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BasketServiceTest {

    @Mock
    private  BasketRepo basketRepo;
    @Mock
    private  ProductRepo productRepo;
    @InjectMocks
    private BasketService basketService;

    @Test
    void shouldAddProductToBasketWhenBasketIdNotExists() {
        //given
        Long basketId = 0L;
        BasketProductDto basketProductDto = new BasketProductDto(1L, 1L);
        when(productRepo.findById(1L)).thenReturn(Optional.of(Product.builder().id(1L).build()));
        when(basketRepo.save(any())).thenReturn(Basket.builder().id(1L).build());
        //when
        Basket basket = basketService.addProductToBasket(basketId, basketProductDto);

        //then
        assertThat(basket).isNotNull();
        assertThat(basket.getId()).isEqualTo(1L);
        assertEquals(1, basket.getItems().size());

    }

    @Test
    void shouldAddProductToBasketWhenBasketIdExists() {
        //given
        Long basketId = 1L;
        BasketProductDto basketProductDto = new BasketProductDto(1L, 1L);
        when(productRepo.findById(1L)).thenReturn(Optional.of(Product.builder().id(1L).build()));
        when(basketRepo.findById(basketId)).thenReturn(Optional.of(Basket.builder().id(basketId).build()));
        //when
        Basket basket = basketService.addProductToBasket(basketId, basketProductDto);

        //then
        assertThat(basket).isNotNull();
        assertThat(basket.getId()).isEqualTo(1L);
        assertEquals(1, basket.getItems().size());

    }


}