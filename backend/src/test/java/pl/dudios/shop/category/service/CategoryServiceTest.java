package pl.dudios.shop.category.service;

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
import org.springframework.data.domain.Pageable;
import pl.dudios.shop.category.repository.CategoryRepo;
import pl.dudios.shop.common.dto.ProductDto;
import pl.dudios.shop.common.model.Category;
import pl.dudios.shop.common.model.Product;
import pl.dudios.shop.common.repository.ProductRepo;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * Reguły przeglądania katalogu po kategoriach. Mockowane są wyłącznie repozytoria
 * (granica bazy danych); Category, Product i ProductDto to prawdziwe obiekty domenowe,
 * dzięki czemu testy pilnują tego, co widzi wołający — wybranej kategorii, listy
 * produktów przyciętej do żądanej strony oraz kompletu pól potrzebnych do wyświetlenia
 * kafelka produktu — a nie wewnętrznych kroków serwisu.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    private static final Pageable FIRST_PAGE = PageRequest.of(0, 10);

    @Mock
    private CategoryRepo categoryRepo;
    @Mock
    private ProductRepo productRepo;
    @InjectMocks
    private CategoryService categoryService;

    @Nested
    @DisplayName("listing the categories offered by the shop")
    class ListingCategories {

        @Test
        @DisplayName("hands the caller every category in the catalogue without touching the product table")
        void returnsEveryCategory() {
            //Given
            given(categoryRepo.findAll()).willReturn(List.of(
                    newCategory(1L, "Elektronika", "elektronika"),
                    newCategory(2L, "Ogród", "ogrod")));

            //When
            var categories = categoryService.getAllCategories();

            //Then
            assertThat(categories)
                    .extracting(Category::getId, Category::getSlug)
                    .containsExactly(
                            org.assertj.core.groups.Tuple.tuple(1L, "elektronika"),
                            org.assertj.core.groups.Tuple.tuple(2L, "ogrod"));
            then(productRepo).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("browsing a single category by its slug")
    class BrowsingCategoryBySlug {

        @Test
        @DisplayName("returns the browsed category itself so the listing can render its header")
        void returnsBrowsedCategory() {
            //Given
            var category = newCategory(7L, "Elektronika", "elektronika");
            given(categoryRepo.findBySlug("elektronika")).willReturn(category);
            given(productRepo.findByCategoryId(any(), any()))
                    .willReturn(new PageImpl<>(List.of(), FIRST_PAGE, 0));

            //When
            var result = categoryService.getCategoryWithProducts("elektronika", FIRST_PAGE);

            //Then
            assertThat(result.category().getId()).isEqualTo(7L);
            assertThat(result.category().getSlug()).isEqualTo("elektronika");
            assertThat(result.category().getNameEn()).isEqualTo("Elektronika EN");
        }

        @Test
        @DisplayName("carries over every product field the listing needs, in both language versions")
        void mapsAllProductFieldsNeededByTheListing() {
            //Given
            var category = newCategory(7L, "Elektronika", "elektronika");
            var product = newProduct(42L, "Laptop", "laptop");
            given(categoryRepo.findBySlug("elektronika")).willReturn(category);
            given(productRepo.findByCategoryId(any(), any()))
                    .willReturn(new PageImpl<>(List.of(product), FIRST_PAGE, 1));

            //When
            var result = categoryService.getCategoryWithProducts("elektronika", FIRST_PAGE);

            //Then
            assertThat(result.productsPage().getContent()).containsExactly(
                    ProductDto.builder()
                            .id(42L)
                            .name("Laptop")
                            .description("Opis produktu Laptop")
                            .nameEn("Laptop EN")
                            .descriptionEn("Description of Laptop")
                            .price(new BigDecimal("3499.00"))
                            .currency("PLN")
                            .image("laptop.png")
                            .slug("laptop")
                            .build());
        }

        @Test
        @DisplayName("reports how many products the whole category holds, not just the ones on the current page")
        void reportsTotalNumberOfProductsInCategory() {
            //Given
            var category = newCategory(7L, "Elektronika", "elektronika");
            var firstTwo = List.of(newProduct(1L, "Laptop", "laptop"), newProduct(2L, "Mysz", "mysz"));
            given(categoryRepo.findBySlug("elektronika")).willReturn(category);
            given(productRepo.findByCategoryId(any(), any()))
                    .willReturn(new PageImpl<>(firstTwo, PageRequest.of(0, 2), 7));

            //When
            var result = categoryService.getCategoryWithProducts("elektronika", PageRequest.of(0, 2));

            //Then
            assertThat(result.productsPage().getContent()).hasSize(2);
            assertThat(result.productsPage().getTotalElements()).isEqualTo(7);
            assertThat(result.productsPage().getTotalPages()).isEqualTo(4);
            assertThat(result.productsPage().hasNext()).isTrue();
        }

        @Test
        @DisplayName("returns an empty product page for a category that has nothing on offer yet")
        void returnsEmptyPageForCategoryWithoutProducts() {
            //Given
            var category = newCategory(7L, "Elektronika", "elektronika");
            given(categoryRepo.findBySlug("elektronika")).willReturn(category);
            given(productRepo.findByCategoryId(any(), any()))
                    .willReturn(new PageImpl<>(List.of(), FIRST_PAGE, 0));

            //When
            var result = categoryService.getCategoryWithProducts("elektronika", FIRST_PAGE);

            //Then
            assertThat(result.productsPage().getContent()).isEmpty();
            assertThat(result.productsPage().getTotalElements()).isZero();
            assertThat(result.category().getId()).isEqualTo(7L);
        }

        @Test
        @DisplayName("shows only products of the resolved category and only the page the caller asked for")
        void queriesResolvedCategoryAndRequestedPageOnly() {
            //Given
            var category = newCategory(7L, "Elektronika", "elektronika");
            var requestedPage = PageRequest.of(3, 24);
            given(categoryRepo.findBySlug("elektronika")).willReturn(category);
            given(productRepo.findByCategoryId(any(), any()))
                    .willReturn(new PageImpl<>(List.of(), requestedPage, 0));

            //When
            categoryService.getCategoryWithProducts("elektronika", requestedPage);

            //Then
            var categoryIdCaptor = ArgumentCaptor.forClass(Long.class);
            var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            then(productRepo).should().findByCategoryId(categoryIdCaptor.capture(), pageableCaptor.capture());
            assertThat(categoryIdCaptor.getValue()).isEqualTo(7L);
            assertThat(pageableCaptor.getValue()).isEqualTo(requestedPage);
        }
    }

    private static Category newCategory(Long id, String name, String slug) {
        var category = new Category();
        category.setId(id);
        category.setName(name);
        category.setNameEn(name + " EN");
        category.setDescription("Opis kategorii " + name);
        category.setDescriptionEn("Description of " + name);
        category.setSlug(slug);
        return category;
    }

    private static Product newProduct(Long id, String name, String slug) {
        return Product.builder()
                .id(id)
                .name(name)
                .categoryId(7L)
                .description("Opis produktu " + name)
                .fullDescription("Bardzo długi opis, którego lista nie potrzebuje")
                .nameEn(name + " EN")
                .descriptionEn("Description of " + name)
                .fullDescriptionEn("Very long description the listing does not need")
                .price(new BigDecimal("3499.00"))
                .currency("PLN")
                .image("laptop.png")
                .slug(slug)
                .build();
    }
}
