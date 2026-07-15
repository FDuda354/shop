package pl.dudios.shopmvn.product.controller;

import lombok.AllArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pl.dudios.shopmvn.common.dto.ProductDto;
import pl.dudios.shopmvn.common.model.Product;
import pl.dudios.shopmvn.product.service.ProductService;

import jakarta.validation.constraints.Pattern;
import java.util.List;

@RestController
@AllArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;

    @GetMapping("/products")
    public Page<ProductDto> getProducts(@PageableDefault(size = 5) Pageable pageable) {
        Page<Product> pageOfProducts = productService.getProducts(pageable);
        List<ProductDto> productsDto = pageOfProducts.getContent().stream()
                .map(product ->
                        ProductDto.builder()
                                .id(product.getId())
                                .name(product.getName())
                                .description(product.getDescription())
                                .price(product.getPrice())
                                .currency(product.getCurrency())
                                .image(product.getImage())
                                .slug(product.getSlug())
                                .build())
                .toList();

        return new PageImpl<>(productsDto, pageable, pageOfProducts.getTotalElements());
    }

    @GetMapping("/product/{slug}")
    public Product getProductBySlug(
            @PathVariable
            @Pattern(regexp = "[a-z0-9\\-]+")
            @Length(min = 3, max = 255)
            String slug) {
        return productService.getProductBySlug(slug);
    }

}
