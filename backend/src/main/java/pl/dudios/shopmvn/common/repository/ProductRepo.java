package pl.dudios.shopmvn.common.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dudios.shopmvn.common.model.Product;

import java.util.Optional;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {

    Optional<Product> findBySlug(String slug);

    Page<Product> findByCategoryId(Long id, Pageable pageable);
}
