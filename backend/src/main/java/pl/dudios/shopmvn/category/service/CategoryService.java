package pl.dudios.shopmvn.category.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dudios.shopmvn.category.dto.CategoryProductDto;
import pl.dudios.shopmvn.category.repository.CategoryRepo;
import pl.dudios.shopmvn.common.dto.ProductDto;
import pl.dudios.shopmvn.common.model.Category;
import pl.dudios.shopmvn.common.model.Product;
import pl.dudios.shopmvn.common.repository.ProductRepo;

import java.util.List;

@Service
@AllArgsConstructor
public class CategoryService {

    private final CategoryRepo categoryRepo;
    private final ProductRepo productRepo;

    public List<Category> getAllCategories() {
        return categoryRepo.findAll();
    }

    @Transactional(readOnly = true)
    public CategoryProductDto getCategoryWithProducts(String slug, Pageable pageable) {
        Category category = categoryRepo.findBySlug(slug);
        Page<Product> productPage = productRepo.findByCategoryId(category.getId(), pageable);
        List<ProductDto> productsDto = productPage.getContent().stream()
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
        return new CategoryProductDto(category, new PageImpl<>(productsDto, pageable, productPage.getTotalElements()));
    }

}
