package pl.dudios.shopmvn.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dudios.shopmvn.common.model.Category;

@Repository
public interface CategoryRepo extends JpaRepository<Category, Long> {

    //    @Query("SELECT c FROM Category c " +
//            "LEFT JOIN FETCH c.products p " +
//            "WHERE c.slug = :slug " +
//            "ORDER BY p.name")
    Category findBySlug(String slug);
}
