package pl.dudios.shopmvn.admin.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.dudios.shopmvn.admin.product.model.AdminProduct;

import java.util.List;

public interface AdminProductRepo extends JpaRepository<AdminProduct, Long> {
    List<AdminProduct> findAllByCategoryId(Long id);
}
