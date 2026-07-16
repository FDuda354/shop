package pl.dudios.shop.admin.product.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Czysta logika nadawania unikalnych nazw plików — bez mocków, kolizje
 * symuluje zwykły Set nazw już zajętych.
 */
class ExistingFileRenameUtilsTest {

    @Nested
    @DisplayName("when the name is free")
    class WhenNameIsFree {

        @Test
        @DisplayName("keeps the original file name untouched")
        void keepsOriginalName() {
            //Given
            var taken = Set.<String>of();

            //When
            var result = ExistingFileRenameUtils.renameFileIfExists(taken::contains, "test.png");

            //Then
            assertThat(result).isEqualTo("test.png");
        }
    }

    @Nested
    @DisplayName("when the name collides with an existing image")
    class WhenNameCollides {

        @Test
        @DisplayName("appends -1 suffix on the first collision")
        void appendsSuffixOnFirstCollision() {
            //Given
            var taken = Set.of("test.png");

            //When
            var result = ExistingFileRenameUtils.renameFileIfExists(taken::contains, "test.png");

            //Then
            assertThat(result).isEqualTo("test-1.png");
        }

        @Test
        @DisplayName("increments the suffix until a free name is found")
        void incrementsSuffixUntilFree() {
            //Given
            var taken = Set.of("test.png", "test-1.png", "test-2.png", "test-3.png");

            //When
            var result = ExistingFileRenameUtils.renameFileIfExists(taken::contains, "test.png");

            //Then
            assertThat(result).isEqualTo("test-4.png");
        }

        @Test
        @DisplayName("continues numbering from an already suffixed name")
        void continuesNumberingFromSuffixedName() {
            //Given
            var taken = Set.of("test-7.png");

            //When
            var result = ExistingFileRenameUtils.renameFileIfExists(taken::contains, "test-7.png");

            //Then
            assertThat(result).isEqualTo("test-8.png");
        }
    }
}
