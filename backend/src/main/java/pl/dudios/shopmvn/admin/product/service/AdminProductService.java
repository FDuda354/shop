package pl.dudios.shopmvn.admin.product.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.dudios.shopmvn.admin.product.model.AdminProduct;
import pl.dudios.shopmvn.admin.product.repository.AdminProductRepo;

@Service
@AllArgsConstructor
public class AdminProductService {
    private final AdminProductRepo adminProductRepo;

    public Page<AdminProduct> getProducts(Pageable pageable) {
        return adminProductRepo.findAll(pageable);
    }

    public AdminProduct getProduct(Long id) {
        return adminProductRepo.findById(id).orElseThrow();
    }

    public AdminProduct addProduct(AdminProduct product) {
        return adminProductRepo.save(product);
    }

    public AdminProduct updateProduct(AdminProduct product) {
        return adminProductRepo.save(product);
    }

    public void deleteProduct(Long id) {
        adminProductRepo.deleteById(id);
    }

}
