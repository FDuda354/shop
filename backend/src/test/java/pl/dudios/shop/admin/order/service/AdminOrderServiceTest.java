package pl.dudios.shop.admin.order.service;

import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.data.domain.Sort;
import pl.dudios.shop.admin.order.model.AdminOrder;
import pl.dudios.shop.admin.order.model.log.AdminOrderLog;
import pl.dudios.shop.admin.order.repository.AdminOrderLogRepo;
import pl.dudios.shop.admin.order.repository.AdminOrderRepo;
import pl.dudios.shop.common.model.OrderStatus;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * Reguły panelu administratora dotyczące zamówień: listowanie zawsze od najnowszych,
 * pobranie pojedynczego zamówienia oraz zmiana statusu wraz z jej skutkami ubocznymi
 * (wpis do dziennika zmian i powiadomienie klienta).
 * <p>
 * Mockowane są wyłącznie granice systemu: repozytoria (DB) oraz AdminOrderEmailMessage
 * (wysyłka maila). Encje AdminOrder / AdminOrderLog i enum OrderStatus to prawdziwe
 * obiekty domenowe, dzięki czemu testy sprawdzają to, co widzi wołający — stan zamówienia,
 * treść wpisu w dzienniku, fakt (lub brak) powiadomienia — a nie kroki implementacji.
 */
@ExtendWith(MockitoExtension.class)
class AdminOrderServiceTest {

    @Mock
    private AdminOrderRepo adminOrderRepo;
    @Mock
    private AdminOrderLogRepo adminOrderLogRepo;
    @Mock
    private AdminOrderEmailMessage adminOrderEmailMessage;
    @InjectMocks
    private AdminOrderService adminOrderService;

    @Nested
    @DisplayName("listing orders in the admin panel")
    class ListingOrders {

        @Test
        @DisplayName("hands the caller back exactly the orders the repository found")
        void returnsOrdersFoundByRepository() {
            //Given
            var order = newOrder(7L, OrderStatus.NEW);
            given(adminOrderRepo.findAll(any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(order)));

            //When
            var result = adminOrderService.getOrders(PageRequest.of(0, 10));

            //Then
            assertThat(result.getContent()).containsExactly(order);
        }

        @Test
        @DisplayName("always shows the newest orders first, overriding the sort the caller asked for")
        void alwaysSortsNewestFirst() {
            //Given
            var callerSort = PageRequest.of(0, 10, Sort.by("placeDate").ascending());
            given(adminOrderRepo.findAll(any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of()));

            //When
            adminOrderService.getOrders(callerSort);

            //Then
            var used = capturePageable();
            assertThat(used.getSort().getOrderFor("placeDate")).isNull();
            assertThat(used.getSort().getOrderFor("id")).isNotNull();
            assertThat(used.getSort().getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.DESC);
        }

        @Test
        @DisplayName("respects the page number and page size the caller asked for")
        void respectsRequestedPaging() {
            //Given
            given(adminOrderRepo.findAll(any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of()));

            //When
            adminOrderService.getOrders(PageRequest.of(3, 25));

            //Then
            var used = capturePageable();
            assertThat(used.getPageNumber()).isEqualTo(3);
            assertThat(used.getPageSize()).isEqualTo(25);
        }

        private Pageable capturePageable() {
            var captor = ArgumentCaptor.forClass(Pageable.class);
            then(adminOrderRepo).should().findAll(captor.capture());
            return captor.getValue();
        }
    }

    @Nested
    @DisplayName("fetching a single order")
    class FetchingSingleOrder {

        @Test
        @DisplayName("returns the order stored under the requested id")
        void returnsOrderById() {
            //Given
            var order = newOrder(7L, OrderStatus.PAID);
            given(adminOrderRepo.findById(7L)).willReturn(Optional.of(order));

            //When
            var result = adminOrderService.getOrder(7L);

            //Then
            assertThat(result).isSameAs(order);
        }

        @Test
        @DisplayName("fails instead of returning null when the order does not exist")
        void failsForUnknownOrder() {
            //Given
            given(adminOrderRepo.findById(404L)).willReturn(Optional.empty());

            //When //Then
            assertThatThrownBy(() -> adminOrderService.getOrder(404L))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("changing the status of an order")
    class ChangingOrderStatus {

        private AdminOrder order;

        @BeforeEach
        void setUp() {
            order = newOrder(7L, OrderStatus.NEW);
            given(adminOrderRepo.findById(7L)).willReturn(Optional.of(order));
        }

        @Test
        @DisplayName("stores the requested status on the order")
        void storesNewStatus() {
            //Given
            var payload = Map.of("orderStatus", "PAID");

            //When
            adminOrderService.updateOrderStatus(7L, payload);

            //Then
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID);
            then(adminOrderRepo).should().save(order);
        }

        @Test
        @DisplayName("records an audit entry naming the order and both the old and the new status")
        void recordsAuditEntry() {
            //Given
            var payload = Map.of("orderStatus", "PAID");

            //When
            adminOrderService.updateOrderStatus(7L, payload);

            //Then
            var captor = ArgumentCaptor.forClass(AdminOrderLog.class);
            then(adminOrderLogRepo).should().save(captor.capture());
            var log = captor.getValue();
            assertThat(log.getOrderId()).isEqualTo(7L);
            assertThat(log.getCreated()).isNotNull();
            assertThat(log.getNote()).contains("NEW").contains("PAID");
        }

        @Test
        @DisplayName("notifies the client about the status the order has just reached")
        void notifiesClient() {
            //Given
            var payload = Map.of("orderStatus", "COMPLETED");

            //When
            adminOrderService.updateOrderStatus(7L, payload);

            //Then
            then(adminOrderEmailMessage).should().notifyClient(OrderStatus.COMPLETED, order);
        }
    }

    @Nested
    @DisplayName("requests that must leave the order exactly as it was")
    class RequestsWithoutEffect {

        private AdminOrder order;

        @BeforeEach
        void setUp() {
            order = newOrder(7L, OrderStatus.NEW);
            given(adminOrderRepo.findById(7L)).willReturn(Optional.of(order));
        }

        @Test
        @DisplayName("ignores a payload that carries no status at all")
        void ignoresPayloadWithoutStatus() {
            //Given
            var payload = Map.of("firstName", "Anna");

            //When
            adminOrderService.updateOrderStatus(7L, payload);

            //Then
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.NEW);
            then(adminOrderRepo).should(never()).save(any());
            then(adminOrderLogRepo).shouldHaveNoInteractions();
            then(adminOrderEmailMessage).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("does not log or e-mail again when the order already has the requested status")
        void skipsRepeatedStatus() {
            //Given
            var payload = Map.of("orderStatus", "NEW");

            //When
            adminOrderService.updateOrderStatus(7L, payload);

            //Then
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.NEW);
            then(adminOrderRepo).should(never()).save(any());
            then(adminOrderLogRepo).shouldHaveNoInteractions();
            then(adminOrderEmailMessage).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("refuses a status name that does not exist and keeps the current status")
        void refusesUnknownStatusName() {
            //Given
            var payload = Map.of("orderStatus", "NOT_A_REAL_STATUS");

            //When //Then
            assertThatThrownBy(() -> adminOrderService.updateOrderStatus(7L, payload))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("NOT_A_REAL_STATUS");

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.NEW);
            then(adminOrderRepo).should(never()).save(any());
            then(adminOrderLogRepo).shouldHaveNoInteractions();
            then(adminOrderEmailMessage).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("changing the status of an order that does not exist")
    class ChangingStatusOfMissingOrder {

        @Test
        @DisplayName("fails and touches neither the audit log nor the client")
        void failsWithoutSideEffects() {
            //Given
            given(adminOrderRepo.findById(404L)).willReturn(Optional.empty());

            //When //Then
            assertThatThrownBy(() -> adminOrderService.updateOrderStatus(404L, Map.of("orderStatus", "PAID")))
                    .isInstanceOf(NoSuchElementException.class);

            then(adminOrderRepo).should(never()).save(any());
            then(adminOrderLogRepo).shouldHaveNoInteractions();
            then(adminOrderEmailMessage).shouldHaveNoInteractions();
        }
    }

    private static AdminOrder newOrder(Long id, OrderStatus status) {
        var order = new AdminOrder();
        order.setId(id);
        order.setOrderStatus(status);
        order.setFirstName("Anna");
        order.setLastName("Nowak");
        order.setEmail("anna.nowak@example.com");
        return order;
    }
}
