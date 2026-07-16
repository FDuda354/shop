package pl.dudios.shop.admin.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.dudios.shop.admin.product.model.AdminProduct;

import java.util.List;

public interface AdminProductRepo extends JpaRepository<AdminProduct, Long> {
    List<AdminProduct> findAllByCategoryId(Long id);
}
