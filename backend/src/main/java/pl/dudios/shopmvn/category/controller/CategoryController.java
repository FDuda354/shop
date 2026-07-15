package pl.dudios.shopmvn.category.controller;

import lombok.AllArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pl.dudios.shopmvn.category.dto.CategoryProductDto;
import pl.dudios.shopmvn.category.service.CategoryService;
import pl.dudios.shopmvn.common.model.Category;

import jakarta.validation.constraints.Pattern;
import java.util.List;

@RestController
@AllArgsConstructor
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/categories")
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/category/{slug}/products")
    public CategoryProductDto getCategoryWithProducts(
            @PathVariable
            @Pattern(regexp = "[a-z0-9\\-]+")
            @Length(min = 3, max = 255)
            String slug, Pageable pageable) {
        return categoryService.getCategoryWithProducts(slug, pageable);
    }

}
