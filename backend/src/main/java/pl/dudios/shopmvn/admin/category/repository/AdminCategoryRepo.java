package pl.dudios.shopmvn.admin.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dudios.shopmvn.admin.category.model.AdminCategory;

@Repository
public interface AdminCategoryRepo extends JpaRepository<AdminCategory, Long> {

}
