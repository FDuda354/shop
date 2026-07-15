package pl.dudios.shopmvn.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dudios.shopmvn.common.model.ProductImage;

import java.util.Optional;

@Repository
public interface ProductImageRepo extends JpaRepository<ProductImage, Long> {

    Optional<ProductImage> findByName(String name);

    boolean existsByName(String name);
}
