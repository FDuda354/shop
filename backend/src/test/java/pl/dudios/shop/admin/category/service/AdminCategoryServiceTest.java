package pl.dudios.shop.admin.category.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dudios.shop.admin.category.model.AdminCategory;
import pl.dudios.shop.admin.category.repository.AdminCategoryRepo;
import pl.dudios.shop.admin.product.model.AdminProduct;
import pl.dudios.shop.admin.product.repository.AdminProductRepo;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

/**
 * Reguły administracyjnego zarządzania kategoriami sklepu.
 * <p>
 * Mockowane są wyłącznie repozytoria (granica bazy danych) — encje
 * {@link AdminCategory} i {@link AdminProduct} to prawdziwe obiekty domenowe.
 * <p>
 * Najistotniejszą regułą biznesową klasy jest kasowanie kategorii: produkty
 * skasowanej kategorii NIE giną razem z nią, tylko trafiają do kategorii
 * domyślnej (id = 1), i to zanim kategoria zniknie — schemat ma
 * {@code CONSTRAINT fk_categories FOREIGN KEY (category_id) REFERENCES categories(id)}
 * przy {@code category_id NOT NULL}, więc odwrotna kolejność wysadziłaby bazę.
 * Pozostałe metody sprawdzamy pod kątem kontraktu widocznego dla wołającego
 * (zwrócona encja, nadany id, rzucony wyjątek).
 */
@ExtendWith(MockitoExtension.class)
class AdminCategoryServiceTest {

    private static final Long DEFAULT_CATEGORY_ID = 1L;

    @Mock
    private AdminCategoryRepo adminCategoryRepo;
    @Mock
    private AdminProductRepo productRepo;
    @InjectMocks
    private AdminCategoryService adminCategoryService;

    @Nested
    @DisplayName("listing categories for the admin panel")
    class ListingCategories {

        @Test
        @DisplayName("returns every stored category, unfiltered")
        void returnsEveryStoredCategory() {
            //Given
            given(adminCategoryRepo.findAll()).willReturn(List.of(category(1L, "ZBOŻA"), category(2L, "NABIAŁ")));

            //When
            var categories = adminCategoryService.getAllCategories();

            //Then
            assertThat(categories)
                    .extracting(AdminCategory::getId, AdminCategory::getName)
                    .containsExactly(
                            org.assertj.core.groups.Tuple.tuple(1L, "ZBOŻA"),
                            org.assertj.core.groups.Tuple.tuple(2L, "NABIAŁ"));
        }
    }

    @Nested
    @DisplayName("fetching a single category")
    class FetchingSingleCategory {

        @Test
        @DisplayName("returns the category stored under the requested id")
        void returnsCategoryStoredUnderRequestedId() {
            //Given
            given(adminCategoryRepo.findById(3L)).willReturn(Optional.of(category(3L, "OWOCE")));

            //When
            var category = adminCategoryService.getCategory(3L);

            //Then
            assertThat(category.getId()).isEqualTo(3L);
            assertThat(category.getName()).isEqualTo("OWOCE");
        }

        @Test
        @DisplayName("fails instead of returning null when the id matches no category")
        void failsWhenIdMatchesNoCategory() {
            //Given
            given(adminCategoryRepo.findById(404L)).willReturn(Optional.empty());

            //When //Then
            assertThatThrownBy(() -> adminCategoryService.getCategory(404L))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("adding a category")
    class AddingCategory {

        @Test
        @DisplayName("persists the submitted category and hands back the id assigned by the database")
        void persistsSubmittedCategoryAndReturnsGeneratedId() {
            //Given
            var submitted = AdminCategory.builder().name("PRZYPRAWY").slug("przyprawy").build();
            given(adminCategoryRepo.save(any(AdminCategory.class))).willReturn(category(9L, "PRZYPRAWY"));

            //When
            var saved = adminCategoryService.addCategory(submitted);

            //Then
            assertThat(saved.getId()).isEqualTo(9L);
            var captor = ArgumentCaptor.forClass(AdminCategory.class);
            then(adminCategoryRepo).should().save(captor.capture());
            assertThat(captor.getValue().getId()).isNull();
            assertThat(captor.getValue().getName()).isEqualTo("PRZYPRAWY");
        }
    }

    @Nested
    @DisplayName("updating a category")
    class UpdatingCategory {

        @Test
        @DisplayName("saves the edit under the same id so an update never creates a second category")
        void savesEditUnderSameId() {
            //Given
            var edited = AdminCategory.builder().id(4L).name("WARZYWA I OWOCE").slug("warzywa").build();
            given(adminCategoryRepo.save(any(AdminCategory.class))).willReturn(edited);

            //When
            var updated = adminCategoryService.updateCategory(edited);

            //Then
            assertThat(updated.getId()).isEqualTo(4L);
            var captor = ArgumentCaptor.forClass(AdminCategory.class);
            then(adminCategoryRepo).should().save(captor.capture());
            assertThat(captor.getValue().getId()).isEqualTo(4L);
            assertThat(captor.getValue().getName()).isEqualTo("WARZYWA I OWOCE");
        }
    }

    @Nested
    @DisplayName("deleting a category")
    class DeletingCategory {

        @Test
        @DisplayName("moves every product of the removed category to the default one instead of deleting the products")
        void movesProductsToDefaultCategory() {
            //Given
            given(productRepo.findAllByCategoryId(7L)).willReturn(List.of(
                    product(100L, 7L),
                    product(101L, 7L),
                    product(102L, 7L)));

            //When
            adminCategoryService.deleteCategory(7L);

            //Then
            var savedProducts = ArgumentCaptor.<List<AdminProduct>>captor();
            then(productRepo).should().saveAll(savedProducts.capture());
            assertThat(savedProducts.getValue())
                    .extracting(AdminProduct::getId)
                    .containsExactly(100L, 101L, 102L);
            assertThat(savedProducts.getValue())
                    .extracting(AdminProduct::getCategoryId)
                    .containsOnly(DEFAULT_CATEGORY_ID);
            then(productRepo).should(never()).deleteAll(any());
        }

        @Test
        @DisplayName("re-homes the products before dropping the category, never the other way round")
        void reHomesProductsBeforeDroppingCategory() {
            //Given
            given(productRepo.findAllByCategoryId(7L)).willReturn(List.of(product(100L, 7L)));

            //When
            adminCategoryService.deleteCategory(7L);

            //Then
            // Kolejność jest tu regułą biznesową, nie szczegółem implementacji:
            // products.category_id jest NOT NULL z FK na categories(id), więc
            // skasowanie kategorii przed przepięciem produktów wywala constraint.
            var order = inOrder(productRepo, adminCategoryRepo);
            then(productRepo).should(order).saveAll(any());
            then(adminCategoryRepo).should(order).deleteById(7L);
        }

        @Test
        @DisplayName("removes a category that holds no products without re-homing anything")
        void removesEmptyCategory() {
            //Given
            given(productRepo.findAllByCategoryId(8L)).willReturn(List.of());

            //When
            adminCategoryService.deleteCategory(8L);

            //Then
            var savedProducts = ArgumentCaptor.<List<AdminProduct>>captor();
            then(productRepo).should().saveAll(savedProducts.capture());
            assertThat(savedProducts.getValue()).isEmpty();
            then(adminCategoryRepo).should().deleteById(8L);
        }
    }

    private static AdminCategory category(Long id, String name) {
        return AdminCategory.builder()
                .id(id)
                .name(name)
                .build();
    }

    private static AdminProduct product(Long id, Long categoryId) {
        return AdminProduct.builder()
                .id(id)
                .name("product-" + id)
                .categoryId(categoryId)
                .build();
    }
}
