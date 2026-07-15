package pl.dudios.shopmvn.admin.category.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.dudios.shopmvn.admin.category.model.AdminCategory;
import pl.dudios.shopmvn.admin.category.repository.AdminCategoryRepo;
import pl.dudios.shopmvn.admin.product.model.AdminProduct;
import pl.dudios.shopmvn.admin.product.repository.AdminProductRepo;

import java.util.List;

@Service
@AllArgsConstructor
public class AdminCategoryService {

    private final AdminCategoryRepo adminCategoryRepo;
    private final AdminProductRepo productRepo;

    public List<AdminCategory> getAllCategories() {
        return adminCategoryRepo.findAll();
    }

    public AdminCategory getCategory(Long id) {
        return adminCategoryRepo.findById(id).orElseThrow();
    }

    public AdminCategory addCategory(AdminCategory adminCategory) {
        return adminCategoryRepo.save(adminCategory);
    }

    public AdminCategory updateCategory(AdminCategory adminCategory) {
        return adminCategoryRepo.save(adminCategory);
    }

    //TODO: Optimize this
    public void deleteCategory(Long id) {
        List<AdminProduct> products = productRepo.findAllByCategoryId(id);
        products.forEach(product -> product.setCategoryId(1L));
        productRepo.saveAll(products);
        adminCategoryRepo.deleteById(id);
    }
}
