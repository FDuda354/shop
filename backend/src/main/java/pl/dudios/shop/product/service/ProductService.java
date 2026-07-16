package pl.dudios.shop.product.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.dudios.shop.common.model.Product;
import pl.dudios.shop.common.repository.ProductRepo;


@Service
@AllArgsConstructor
public class ProductService {

    private final ProductRepo productRepo;

    public Page<Product> getProducts(Pageable pageable) {
        return productRepo.findAll(pageable);
    }

    public Product getProductBySlug(String slug) {
        return productRepo.findBySlug(slug).orElseThrow();

    }

}
