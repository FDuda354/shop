package pl.dudios.shopmvn.admin.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import pl.dudios.shopmvn.AbstractIntegrationTest;
import pl.dudios.shopmvn.admin.product.model.dto.UploadResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Zdjęcia produktów w bazie — kontrakt HTTP end-to-end na prawdziwym
 * Postgresie: seedy z migracji Flyway (V3, javowa) muszą być serwowalne,
 * a upload musi wracać byte-for-byte pod zwróconą nazwą.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class ProductImageIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Nested
    @DisplayName("seeded images")
    class SeededImages {

        @Test
        @DisplayName("serves a Flyway-seeded product image with its content type and an immutable cache header")
        void servesSeededImage() {
            //When
            var response = rest.getForEntity("/data/productImages/apple.gif", byte[].class);

            //Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.IMAGE_GIF);
            assertThat(response.getBody()).isNotEmpty();
            // Nazwy obrazków są unikalne (sufiks -N przy kolizji), więc kontrakt
            // agresywnego cache'owania jest częścią zachowania endpointu.
            assertThat(response.getHeaders().getCacheControl())
                    .contains("public")
                    .contains("immutable");
        }

        @Test
        @DisplayName("responds with 404 for an image that was never uploaded")
        void respondsWith404ForUnknownImage() {
            //When
            var response = rest.getForEntity("/data/productImages/no-such-image.gif", String.class);

            //Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("uploading images")
    class UploadingImages {

        @Test
        @DisplayName("uploaded image is retrievable byte-for-byte under the returned name")
        void uploadedImageIsRetrievableByteForByte() {
            //Given
            // Unikalna nazwa per bieg — reużyty kontener (withReuse) trzyma
            // wiersze z poprzednich biegów, a upload dokleiłby im sufiks -N.
            var baseName = uniqueBaseName();
            var bytes = new byte[]{71, 73, 70, 56, 57, 97, 1, 2, 3};

            //When
            var uploadResponse = upload("Roundtrip " + baseName + ".png", bytes);

            //Then
            assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            var savedName = uploadResponse.getBody().fileName();
            assertThat(savedName).isEqualTo("roundtrip-" + baseName + ".png");

            var served = rest.getForEntity("/data/productImage/" + savedName, byte[].class);
            assertThat(served.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(served.getHeaders().getContentType()).isEqualTo(MediaType.IMAGE_PNG);
            assertThat(served.getBody()).isEqualTo(bytes);
        }

        @Test
        @DisplayName("second upload with the same file name gets a unique -N name instead of overwriting")
        void collidingUploadGetsUniqueName() {
            //Given
            var baseName = uniqueBaseName();
            var first = upload("Twice " + baseName + ".png", new byte[]{1});

            //When
            var second = upload("Twice " + baseName + ".png", new byte[]{2});

            //Then
            assertThat(first.getBody().fileName()).isEqualTo("twice-" + baseName + ".png");
            assertThat(second.getBody().fileName()).isEqualTo("twice-" + baseName + "-1.png");

            var servedSecond = rest.getForEntity("/data/productImage/" + second.getBody().fileName(), byte[].class);
            assertThat(servedSecond.getBody()).isEqualTo(new byte[]{2});
        }
    }

    private static String uniqueBaseName() {
        return java.util.UUID.randomUUID().toString();
    }

    private org.springframework.http.ResponseEntity<UploadResponse> upload(String fileName, byte[] bytes) {
        var file = new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };
        var body = new LinkedMultiValueMap<String, Object>();
        body.add("file", file);
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return rest.postForEntity("/profile/upload-image", new HttpEntity<>(body, headers), UploadResponse.class);
    }
}
