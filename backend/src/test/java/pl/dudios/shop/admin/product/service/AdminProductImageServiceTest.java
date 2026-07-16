package pl.dudios.shop.admin.product.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import pl.dudios.shop.common.model.ProductImage;
import pl.dudios.shop.common.repository.ProductImageRepo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * Upload i odczyt zdjęć trzymanych w bazie. Mockowana jest wyłącznie granica
 * systemu (ProductImageRepo); slugifikacja i wybór content-type to prawdziwa
 * logika produkcyjna, więc testy pilnują obserwowalnego kontraktu:
 * zwróconej nazwy pliku oraz tego, co faktycznie ląduje w repozytorium.
 */
@ExtendWith(MockitoExtension.class)
class AdminProductImageServiceTest {

    @Mock
    private ProductImageRepo productImageRepo;
    @InjectMocks
    private AdminProductImageService service;

    @Nested
    @DisplayName("uploading an image")
    class UploadingImage {

        @Test
        @DisplayName("persists the image bytes under a slugified, URL-safe name with a content type")
        void persistsImageUnderSlugifiedName() {
            //Given
            var bytes = new byte[]{1, 2, 3, 4};
            given(productImageRepo.existsByName("my-photo.png")).willReturn(false);

            //When
            var savedName = service.uploadImage("My Photo.png", new ByteArrayInputStream(bytes));

            //Then
            assertThat(savedName).isEqualTo("my-photo.png");
            var captor = ArgumentCaptor.forClass(ProductImage.class);
            then(productImageRepo).should().save(captor.capture());
            var saved = captor.getValue();
            assertThat(saved.getName()).isEqualTo("my-photo.png");
            assertThat(saved.getContentType()).isEqualTo("image/png");
            assertThat(saved.getData()).isEqualTo(bytes);
        }

        @Test
        @DisplayName("stores a colliding upload under the next free -N name instead of overwriting")
        void renamesCollidingUpload() {
            //Given
            given(productImageRepo.existsByName("my-photo.png")).willReturn(true);
            given(productImageRepo.existsByName("my-photo-1.png")).willReturn(false);

            //When
            var savedName = service.uploadImage("My Photo.png", new ByteArrayInputStream(new byte[]{9}));

            //Then
            assertThat(savedName).isEqualTo("my-photo-1.png");
            var captor = ArgumentCaptor.forClass(ProductImage.class);
            then(productImageRepo).should().save(captor.capture());
            assertThat(captor.getValue().getName()).isEqualTo("my-photo-1.png");
        }

        @Test
        @DisplayName("falls back to application/octet-stream for an unrecognized extension")
        void fallsBackToOctetStreamForUnknownExtension() {
            //Given
            given(productImageRepo.existsByName("weird.unknownext")).willReturn(false);

            //When
            service.uploadImage("weird.unknownext", new ByteArrayInputStream(new byte[]{1}));

            //Then
            var captor = ArgumentCaptor.forClass(ProductImage.class);
            then(productImageRepo).should().save(captor.capture());
            assertThat(captor.getValue().getContentType()).isEqualTo("application/octet-stream");
        }

        @Test
        @DisplayName("persists nothing when the upload stream cannot be read")
        void persistsNothingWhenStreamUnreadable() {
            //Given
            given(productImageRepo.existsByName("broken.png")).willReturn(false);
            var brokenStream = new InputStream() {
                @Override
                public int read() throws IOException {
                    throw new IOException("boom");
                }
            };

            //When //Then
            assertThatThrownBy(() -> service.uploadImage("broken.png", brokenStream))
                    .isInstanceOf(RuntimeException.class)
                    .hasCauseInstanceOf(IOException.class);
            then(productImageRepo).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("serving an image")
    class ServingImage {

        @Test
        @DisplayName("returns the stored image for a known name")
        void returnsStoredImage() {
            //Given
            var image = ProductImage.builder()
                    .name("apple.gif")
                    .contentType("image/gif")
                    .data(new byte[]{7, 7, 7})
                    .build();
            given(productImageRepo.findByName("apple.gif")).willReturn(Optional.of(image));

            //When
            var result = service.getImage("apple.gif");

            //Then
            assertThat(result.getContentType()).isEqualTo("image/gif");
            assertThat(result.getData()).isEqualTo(new byte[]{7, 7, 7});
        }

        @Test
        @DisplayName("responds with 404 when the image does not exist")
        void respondsWith404WhenMissing() {
            //Given
            given(productImageRepo.findByName("missing.png")).willReturn(Optional.empty());

            //When //Then
            assertThatThrownBy(() -> service.getImage("missing.png"))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("missing.png")
                    .extracting(e -> ((ResponseStatusException) e).getStatusCode().value())
                    .isEqualTo(404);
        }
    }
}
