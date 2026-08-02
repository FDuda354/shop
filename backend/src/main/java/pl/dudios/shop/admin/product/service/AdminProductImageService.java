package pl.dudios.shop.admin.product.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.dudios.shop.admin.common.utils.SlugifyUtils;
import pl.dudios.shop.common.model.ProductImage;
import pl.dudios.shop.common.repository.ProductImageRepo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminProductImageService {

    private static final Map<String, MediaType> IMAGE_TYPES_BY_EXTENSION = Map.of(
            "png", MediaType.IMAGE_PNG,
            "jpg", MediaType.IMAGE_JPEG,
            "jpeg", MediaType.IMAGE_JPEG,
            "gif", MediaType.IMAGE_GIF,
            "webp", MediaType.parseMediaType("image/webp"),
            "avif", MediaType.parseMediaType("image/avif")
    );

    private final ProductImageRepo productImageRepo;

    @Transactional
    public String uploadImage(String fileName, InputStream inputStream) {
        MediaType contentType = imageTypeOf(fileName);
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
                .contentType(contentType.toString())
                .data(data)
                .build());
        return newFileName;
    }

    @Transactional(readOnly = true)
    public ProductImage getImage(String fileName) {
        return productImageRepo.findByName(fileName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found: " + fileName));
    }

    public MediaType servableContentType(ProductImage image) {
        return IMAGE_TYPES_BY_EXTENSION.values().stream()
                .filter(allowed -> allowed.toString().equals(image.getContentType()))
                .findFirst()
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
    }

    private static MediaType imageTypeOf(String fileName) {
        String extension = Optional.ofNullable(FilenameUtils.getExtension(fileName))
                .orElse("")
                .toLowerCase(Locale.ROOT);
        MediaType contentType = IMAGE_TYPES_BY_EXTENSION.get(extension);
        if (contentType == null) {
            throw new IllegalArgumentException("Unsupported image type: " + extension);
        }
        return contentType;
    }
}
