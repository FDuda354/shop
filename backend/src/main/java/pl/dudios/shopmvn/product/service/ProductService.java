package pl.dudios.shopmvn.product.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.dudios.shopmvn.common.model.Product;
import pl.dudios.shopmvn.common.repository.ProductRepo;


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
