package pl.dudios.shop.admin.order.controller.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import pl.dudios.shop.admin.order.model.AdminOrder;
import pl.dudios.shop.admin.order.model.dto.AdminOrderDto;
import pl.dudios.shop.common.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Mapowanie strony zamówień na DTO listy zamówień w panelu admina
 * ({@code GET /admin/orders}).
 * <p>
 * Nic nie jest mockowane — klasa jest czystą funkcją, więc testy używają
 * prawdziwych encji {@link AdminOrder} i prawdziwych stron Spring Data.
 * Sprawdzana jest wartość obserwowalna dla wołającego (kontrolera, a przez
 * niego frontu): dane zamówienia przeniesione do DTO oraz metadane
 * stronicowania, na których opiera się paginator w UI — w szczególności to,
 * że licznik wszystkich zamówień pochodzi z bazy, a nie z rozmiaru
 * bieżącej strony.
 */
class AdminOrderMapperTest {

    @Nested
    @DisplayName("copying order data onto the admin list DTO")
    class OrderFields {

        @Test
        @DisplayName("carries the order id, status, placement date and gross value to the caller")
        void carriesOrderDataToTheDto() {
            //Given
            var placedAt = LocalDateTime.of(2026, 3, 14, 9, 30);
            var source = singleOrderPage(order(42L, OrderStatus.PAID, placedAt, new BigDecimal("1299.99")));

            //When
            var result = AdminOrderMapper.mapToPageDtos(source);

            //Then
            var dto = result.getContent().getFirst();
            assertThat(dto.id()).isEqualTo(42L);
            assertThat(dto.orderStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(dto.placeDate()).isEqualTo(placedAt);
            assertThat(dto.grossValue()).isEqualTo(new BigDecimal("1299.99"));
        }

        @Test
        @DisplayName("keeps the gross value scale untouched so the admin sees the exact amount charged")
        void keepsGrossValueScale() {
            //Given
            var source = singleOrderPage(order(7L, OrderStatus.NEW, LocalDateTime.now(), new BigDecimal("50.00")));

            //When
            var result = AdminOrderMapper.mapToPageDtos(source);

            //Then
            assertThat(result.getContent().getFirst().grossValue().toPlainString()).isEqualTo("50.00");
        }

        @Test
        @DisplayName("maps every order on the page and preserves the order returned by the repository")
        void mapsEveryOrderInRepositoryOrder() {
            //Given
            var source = new PageImpl<>(List.of(
                    order(3L, OrderStatus.NEW, LocalDateTime.of(2026, 1, 3, 8, 0), new BigDecimal("10.00")),
                    order(1L, OrderStatus.COMPLETED, LocalDateTime.of(2026, 1, 1, 8, 0), new BigDecimal("20.00")),
                    order(2L, OrderStatus.CANCELLED, LocalDateTime.of(2026, 1, 2, 8, 0), new BigDecimal("30.00"))),
                    PageRequest.of(0, 10), 3);

            //When
            var result = AdminOrderMapper.mapToPageDtos(source);

            //Then
            assertThat(result.getContent())
                    .extracting(AdminOrderDto::id, AdminOrderDto::orderStatus)
                    .containsExactly(
                            tuple(3L, OrderStatus.NEW),
                            tuple(1L, OrderStatus.COMPLETED),
                            tuple(2L, OrderStatus.CANCELLED));
        }
    }

    @Nested
    @DisplayName("preserving pagination metadata for the admin paginator")
    class PaginationMetadata {

        @Test
        @DisplayName("reports the total number of orders in the shop, not the number shown on this page")
        void reportsTotalFromTheSourcePage() {
            //Given
            var source = secondPageOfFiveOrders();

            //When
            var result = AdminOrderMapper.mapToPageDtos(source);

            //Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(5L);
            assertThat(result.getTotalPages()).isEqualTo(3);
        }

        @Test
        @DisplayName("stays on the page the admin actually requested")
        void staysOnTheRequestedPage() {
            //Given
            var source = secondPageOfFiveOrders();

            //When
            var result = AdminOrderMapper.mapToPageDtos(source);

            //Then
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(2);
            assertThat(result.getPageable()).isEqualTo(PageRequest.of(1, 2));
        }

        @Test
        @DisplayName("returns an empty page instead of failing when the shop has no orders")
        void returnsEmptyPageWhenNoOrders() {
            //Given
            var source = new PageImpl<AdminOrder>(List.of(), PageRequest.of(0, 20), 0);

            //When
            var result = AdminOrderMapper.mapToPageDtos(source);

            //Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getSize()).isEqualTo(20);
        }

        @Test
        @DisplayName("still reports the real total when the admin pages past the last order")
        void reportsTotalOnAPageBeyondTheEnd() {
            //Given
            var source = new PageImpl<AdminOrder>(List.of(), PageRequest.of(9, 2), 5);

            //When
            var result = AdminOrderMapper.mapToPageDtos(source);

            //Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(5L);
        }
    }

    private static Page<AdminOrder> singleOrderPage(AdminOrder order) {
        return new PageImpl<>(List.of(order), PageRequest.of(0, 10), 1);
    }

    private static Page<AdminOrder> secondPageOfFiveOrders() {
        return new PageImpl<>(List.of(
                order(3L, OrderStatus.PROCESSING, LocalDateTime.of(2026, 2, 3, 12, 0), new BigDecimal("15.00")),
                order(4L, OrderStatus.REFUND, LocalDateTime.of(2026, 2, 4, 12, 0), new BigDecimal("25.00"))),
                PageRequest.of(1, 2), 5);
    }

    private static AdminOrder order(Long id, OrderStatus status, LocalDateTime placeDate, BigDecimal grossValue) {
        var order = new AdminOrder();
        order.setId(id);
        order.setOrderStatus(status);
        order.setPlaceDate(placeDate);
        order.setGrossValue(grossValue);
        return order;
    }
}
