package pl.dudios.shop.category.dto;

import org.springframework.data.domain.Page;
import pl.dudios.shop.common.dto.ProductDto;
import pl.dudios.shop.common.model.Category;

public record CategoryProductDto(Category category, Page<ProductDto> productsPage) {
}

