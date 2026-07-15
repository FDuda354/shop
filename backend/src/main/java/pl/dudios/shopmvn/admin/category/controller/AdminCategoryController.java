package pl.dudios.shopmvn.admin.category.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.dudios.shopmvn.admin.category.model.AdminCategory;
import pl.dudios.shopmvn.admin.category.model.dto.AdminCategoryDto;
import pl.dudios.shopmvn.admin.category.service.AdminCategoryService;

import java.util.List;

import static pl.dudios.shopmvn.admin.common.utils.SlugifyUtils.slugifySlug;

@RestController
@AllArgsConstructor
@RequestMapping("/admin")
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    public static final Long EMPTY_ID = null;

    @GetMapping("/categories")
    public List<AdminCategory> getAllCategories() {
        return adminCategoryService.getAllCategories();
    }

    @GetMapping("/category/{id}")
    public AdminCategory getCategory(@PathVariable Long id) {
        return adminCategoryService.getCategory(id);
    }

    @PostMapping("/category")
    public AdminCategory addCategory(@RequestBody AdminCategoryDto adminCategoryDto) {
        return adminCategoryService.addCategory(mapToaAdminCategory(EMPTY_ID, adminCategoryDto));
    }

    @PutMapping("/category/{id}")
    public AdminCategory updateCategory(@PathVariable Long id, @RequestBody AdminCategoryDto adminCategoryDto) {
        return adminCategoryService.updateCategory(mapToaAdminCategory(id, adminCategoryDto));
    }

    @DeleteMapping("/category/{id}")
    public void deleteCategory(@PathVariable Long id) {
        adminCategoryService.deleteCategory(id);
    }

    private AdminCategory mapToaAdminCategory(Long id, AdminCategoryDto adminCategoryDto) {
        return AdminCategory.builder()
                .id(id)
                .name(adminCategoryDto.getName())
                .description(adminCategoryDto.getDescription())
                .slug(slugifySlug(adminCategoryDto.getSlug()))
                .build();
    }

}
