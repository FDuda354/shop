package pl.dudios.shopmvn.admin.product.controller;

import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pl.dudios.shopmvn.admin.product.model.AdminProduct;
import pl.dudios.shopmvn.admin.product.model.dto.AdminProductDto;
import pl.dudios.shopmvn.admin.product.model.dto.UploadResponse;
import pl.dudios.shopmvn.admin.product.service.AdminProductImageService;
import pl.dudios.shopmvn.admin.product.service.AdminProductService;

import jakarta.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static pl.dudios.shopmvn.admin.common.utils.SlugifyUtils.slugifySlug;

@RestController
@AllArgsConstructor
@Validated
public class AdminProductController {
    public static final Long EMPTY_ID = null;
    private final AdminProductService adminProductService;
    private final AdminProductImageService adminProductImageService;

    @GetMapping("/admin/products")
    public Page<AdminProduct> getProducts(Pageable pageable) {
        return adminProductService.getProducts(pageable);
    }

    @GetMapping("/admin/product/{id}")
    public AdminProduct getProduct(@PathVariable Long id) {
        return adminProductService.getProduct(id);
    }

    @PostMapping("/admin/product")
    public AdminProduct addProduct(@RequestBody @Valid AdminProductDto adminProductDto) {
        return adminProductService.addProduct(mapToAdminProduct(adminProductDto, EMPTY_ID));
    }

    @PutMapping("/admin/product/{id}")
    public AdminProduct updateProduct(@PathVariable Long id, @RequestBody @Valid AdminProductDto adminProductDto) {
        return adminProductService.updateProduct(mapToAdminProduct(adminProductDto, id));
    }

    @DeleteMapping("/admin/product/{id}")
    public void deleteProduct(@PathVariable Long id) {
        adminProductService.deleteProduct(id);
    }

    @PostMapping("/admin/product/upload-image")
    public UploadResponse uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            String savedFileName = adminProductImageService.uploadImage(file.getOriginalFilename(), inputStream);
            return new UploadResponse(savedFileName);
        } catch (IOException e) {
            throw new RuntimeException("Error while saving file " + e.getMessage());
        }

    }

    @PostMapping("/profile/upload-image")
    public UploadResponse uploadProfileImage(@RequestParam("file") MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            String savedFileName = adminProductImageService.uploadImage(file.getOriginalFilename(), inputStream);
            return new UploadResponse(savedFileName);
        } catch (IOException e) {
            throw new RuntimeException("Error while saving file " + e.getMessage());
        }

    }

    @GetMapping("/data/productImage/{fileName}")
    public ResponseEntity<Resource> serveFiles(@PathVariable String fileName) {
        Resource file = adminProductImageService.serveFiles(fileName);
        try {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(Path.of(fileName)))
                    .body(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private AdminProduct mapToAdminProduct(AdminProductDto adminProductDto, Long id) {
        return AdminProduct.builder()
                .id(id)
                .name(adminProductDto.getName())
                .price(adminProductDto.getPrice())
                .categoryId(adminProductDto.getCategoryId())
                .description(adminProductDto.getDescription())
                .fullDescription(Optional.ofNullable(adminProductDto.getFullDescription()).orElse(""))
                .currency(adminProductDto.getCurrency())
                .image(adminProductDto.getImage())
                .slug(slugifySlug(adminProductDto.getSlug()))
                .build();
    }


}
