package pl.dudios.shopmvn.category.dto;

import org.springframework.data.domain.Page;
import pl.dudios.shopmvn.common.dto.ProductDto;
import pl.dudios.shopmvn.common.model.Category;

public record CategoryProductDto(Category category, Page<ProductDto> productsPage) {
}

