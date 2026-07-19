package pl.dudios.shop.admin.product.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import pl.dudios.shop.admin.product.model.AdminProduct;
import pl.dudios.shop.admin.product.model.AdminProductCurrency;
import pl.dudios.shop.admin.product.repository.AdminProductRepo;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * Obsługa katalogu produktów po stronie panelu administracyjnego.
 * Mockowana jest wyłącznie granica systemu (AdminProductRepo); AdminProduct
 * i AdminProductCurrency to prawdziwe obiekty domenowe.
 * <p>
 * Serwis jest cienką warstwą nad repozytorium, więc testy pilnują tego, co
 * naprawdę obserwuje wołający (kontroler admina): że żądana strona wyników
 * dociera do bazy, że brak produktu kończy się wyjątkiem zamiast nulla,
 * że żadne pole wypełnione przez admina nie ginie po drodze do zapisu oraz
 * że edycja zachowuje identyfikator (aktualizacja, a nie nowy wiersz).
 */
@ExtendWith(MockitoExtension.class)
class AdminProductServiceTest {

    @Mock
    private AdminProductRepo adminProductRepo;
    @InjectMocks
    private AdminProductService adminProductService;

    @Nested
    @DisplayName("browsing the product catalogue")
    class BrowsingCatalogue {

        @Test
        @DisplayName("returns the page of products the admin actually asked for")
        void returnsRequestedPageOfProducts() {
            //Given
            var pageable = PageRequest.of(2, 5);
            var products = List.of(persistedProduct(11L, "Kubek"), persistedProduct(12L, "Koszulka"));
            given(adminProductRepo.findAll(pageable)).willReturn(new PageImpl<>(products, pageable, 42));

            //When
            var result = adminProductService.getProducts(pageable);

            //Then
            assertThat(result.getContent()).extracting(AdminProduct::getName)
                    .containsExactly("Kubek", "Koszulka");
            assertThat(result.getNumber()).isEqualTo(2);
            assertThat(result.getSize()).isEqualTo(5);
            assertThat(result.getTotalElements()).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("fetching a single product")
    class FetchingSingleProduct {

        @Test
        @DisplayName("returns the stored product for a known id")
        void returnsStoredProduct() {
            //Given
            given(adminProductRepo.findById(11L)).willReturn(Optional.of(persistedProduct(11L, "Kubek")));

            //When
            var result = adminProductService.getProduct(11L);

            //Then
            assertThat(result.getId()).isEqualTo(11L);
            assertThat(result.getName()).isEqualTo("Kubek");
        }

        @Test
        @DisplayName("refuses to hand back an empty product when the id does not exist")
        void refusesUnknownId() {
            //Given
            given(adminProductRepo.findById(404L)).willReturn(Optional.empty());

            //When //Then
            assertThatThrownBy(() -> adminProductService.getProduct(404L))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("adding a product to the catalogue")
    class AddingProduct {

        @Test
        @DisplayName("returns the persisted product so the admin learns its generated id")
        void returnsPersistedProductWithGeneratedId() {
            //Given
            given(adminProductRepo.save(any(AdminProduct.class))).willReturn(persistedProduct(7L, "Kubek"));

            //When
            var result = adminProductService.addProduct(newProduct());

            //Then
            assertThat(result.getId()).isEqualTo(7L);
        }

        @Test
        @DisplayName("persists every field the admin filled in, including the optional English ones")
        void persistsEveryFieldIncludingEnglishOnes() {
            //Given
            var product = newProduct();

            //When
            adminProductService.addProduct(product);

            //Then
            var captor = ArgumentCaptor.forClass(AdminProduct.class);
            then(adminProductRepo).should().save(captor.capture());
            var saved = captor.getValue();
            assertThat(saved.getId()).isNull();
            assertThat(saved.getName()).isEqualTo("Kubek");
            assertThat(saved.getDescription()).isEqualTo("Ceramiczny kubek");
            assertThat(saved.getFullDescription()).isEqualTo("Ceramiczny kubek 330 ml");
            assertThat(saved.getNameEn()).isEqualTo("Mug");
            assertThat(saved.getDescriptionEn()).isEqualTo("Ceramic mug");
            assertThat(saved.getFullDescriptionEn()).isEqualTo("Ceramic mug 330 ml");
            assertThat(saved.getCategoryId()).isEqualTo(3L);
            assertThat(saved.getPrice()).isEqualByComparingTo("49.99");
            assertThat(saved.getCurrency()).isEqualTo(AdminProductCurrency.PLN);
            assertThat(saved.getImage()).isEqualTo("kubek.png");
            assertThat(saved.getSlug()).isEqualTo("kubek");
        }
    }

    @Nested
    @DisplayName("updating an existing product")
    class UpdatingProduct {

        @Test
        @DisplayName("keeps the id so the existing row is edited instead of a duplicate being created")
        void keepsIdSoExistingRowIsEdited() {
            //Given
            var edited = persistedProduct(42L, "Kubek XL");

            //When
            adminProductService.updateProduct(edited);

            //Then
            var captor = ArgumentCaptor.forClass(AdminProduct.class);
            then(adminProductRepo).should().save(captor.capture());
            assertThat(captor.getValue().getId()).isEqualTo(42L);
            assertThat(captor.getValue().getName()).isEqualTo("Kubek XL");
        }
    }

    @Nested
    @DisplayName("removing a product from the catalogue")
    class RemovingProduct {

        @Test
        @DisplayName("removes exactly the requested product and touches nothing else in the catalogue")
        void removesExactlyRequestedProduct() {
            //When
            adminProductService.deleteProduct(42L);

            //Then
            then(adminProductRepo).should().deleteById(42L);
            then(adminProductRepo).shouldHaveNoMoreInteractions();
        }
    }

    private static AdminProduct newProduct() {
        return AdminProduct.builder()
                .name("Kubek")
                .description("Ceramiczny kubek")
                .fullDescription("Ceramiczny kubek 330 ml")
                .nameEn("Mug")
                .descriptionEn("Ceramic mug")
                .fullDescriptionEn("Ceramic mug 330 ml")
                .categoryId(3L)
                .price(new BigDecimal("49.99"))
                .currency(AdminProductCurrency.PLN)
                .image("kubek.png")
                .slug("kubek")
                .build();
    }

    private static AdminProduct persistedProduct(Long id, String name) {
        return AdminProduct.builder()
                .id(id)
                .name(name)
                .categoryId(3L)
                .price(new BigDecimal("49.99"))
                .currency(AdminProductCurrency.PLN)
                .slug("kubek")
                .build();
    }
}
