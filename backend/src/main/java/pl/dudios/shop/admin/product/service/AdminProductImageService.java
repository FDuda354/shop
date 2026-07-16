package pl.dudios.shop.admin.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.dudios.shop.admin.common.utils.SlugifyUtils;
import pl.dudios.shop.common.model.ProductImage;
import pl.dudios.shop.common.repository.ProductImageRepo;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class AdminProductImageService {

    private final ProductImageRepo productImageRepo;

    @Transactional
    public String uploadImage(String fileName, InputStream inputStream) {
        String newFileName = SlugifyUtils.slugifyFileName(fileName);
        newFileName = ExistingFileRenameUtils.renameFileIfExists(productImageRepo::existsByName, newFileName);

        byte[] data;
        try {
            data = inputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Cant save file", e);
        }

        productImageRepo.save(ProductImage.builder()
                .name(newFileName)
                .contentType(contentTypeFor(newFileName))
                .data(data)
                .build());
        return newFileName;
    }

    @Transactional(readOnly = true)
    public ProductImage getImage(String fileName) {
        return productImageRepo.findByName(fileName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found: " + fileName));
    }

    private static String contentTypeFor(String fileName) {
        return MediaTypeFactory.getMediaType(fileName)
                .orElse(MediaType.APPLICATION_OCTET_STREAM)
                .toString();
    }
}
