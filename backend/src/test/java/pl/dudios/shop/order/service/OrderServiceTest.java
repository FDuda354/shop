package pl.dudios.shop.order.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.dudios.shop.common.mail.EmailClientService;
import pl.dudios.shop.common.mail.EmailSender;
import pl.dudios.shop.common.model.Basket;
import pl.dudios.shop.common.model.BasketItem;
import pl.dudios.shop.common.model.OrderStatus;
import pl.dudios.shop.common.model.Product;
import pl.dudios.shop.common.repository.BasketItemRepo;
import pl.dudios.shop.common.repository.BasketRepo;
import pl.dudios.shop.order.model.Order;
import pl.dudios.shop.order.model.OrderRow;
import pl.dudios.shop.order.model.Payment;
import pl.dudios.shop.order.model.Shipment;
import pl.dudios.shop.order.model.dto.OrderDto;
import pl.dudios.shop.order.model.dto.OrderDtoForUser;
import pl.dudios.shop.order.repositroy.OrderRepo;
import pl.dudios.shop.order.repositroy.OrderRowRepo;
import pl.dudios.shop.order.repositroy.PaymentRepo;
import pl.dudios.shop.order.repositroy.ShipmentRepo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

/**
 * Składanie zamówienia z koszyka oraz lista zamówień użytkownika.
 * Mockowane są wyłącznie granice systemu: repozytoria (DB) oraz klient poczty.
 * Encje Basket/BasketItem/Product/Order i mappery to prawdziwe obiekty, więc
 * testy pilnują tego, co widzi wołający: zwróconego podsumowania, wartości
 * brutto zapisanej w bazie, pozycji zamówienia, opróżnienia koszyka i treści
 * maila potwierdzającego.
 * <p>
 * Payment i Shipment nie mają w kodzie produkcyjnym ani buildera, ani setterów,
 * ani konstruktora z argumentami — jedyny sposób na zbudowanie prawdziwej
 * instancji z wypełnioną ceną to ustawienie pól refleksją (tak samo robi
 * Hibernate). To świadomie nie jest mock: to nadal prawdziwy obiekt domenowy.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepo orderRepo;
    @Mock
    private OrderRowRepo orderRowRepo;
    @Mock
    private BasketRepo basketRepo;
    @Mock
    private BasketItemRepo basketItemRepo;
    @Mock
    private ShipmentRepo shipmentRepo;
    @Mock
    private PaymentRepo paymentRepo;
    @Mock
    private EmailClientService emailClientService;
    @Mock
    private EmailSender emailSender;

    @InjectMocks
    private OrderService orderService;

    @Nested
    @DisplayName("placing an order for a filled basket")
    class PlacingOrder {

        @BeforeEach
        void stubHappyPath() {
            var persistedOrder = Order.builder()
                    .id(100L)
                    .placeDate(LocalDateTime.of(2026, 7, 19, 12, 30))
                    .orderStatus(OrderStatus.NEW)
                    .grossValue(new BigDecimal("51.50"))
                    .payment(payment(3L, "Blik", "Pay within 24h"))
                    .email("jan.kowalski@example.com")
                    .build();

            given(basketRepo.findById(7L)).willReturn(Optional.of(filledBasket()));
            given(shipmentRepo.findById(2L)).willReturn(Optional.of(shipment(2L, new BigDecimal("15.00"))));
            given(paymentRepo.findById(3L)).willReturn(Optional.of(payment(3L, "Blik", "Pay within 24h")));
            given(orderRepo.save(any())).willReturn(persistedOrder);
            given(emailClientService.getSender()).willReturn(emailSender);
        }

        @Test
        @DisplayName("returns a summary of the order that was actually persisted")
        void returnsSummaryOfPersistedOrder() {
            //Given
            var orderDto = orderDto();

            //When
            var summary = orderService.createOrder(orderDto, 42L);

            //Then
            assertThat(summary.id()).isEqualTo(100L);
            assertThat(summary.status()).isEqualTo(OrderStatus.NEW);
            assertThat(summary.grossValue()).isEqualByComparingTo("51.50");
            assertThat(summary.payment().getName()).isEqualTo("Blik");
        }

        @Test
        @DisplayName("charges the buyer for every basket line plus the shipment fee")
        void chargesBasketLinesPlusShipment() {
            //Given
            var captor = ArgumentCaptor.forClass(Order.class);

            //When
            orderService.createOrder(orderDto(), 42L);

            //Then
            then(orderRepo).should().save(captor.capture());
            assertThat(captor.getValue().getGrossValue()).isEqualByComparingTo("51.50");
        }

        @Test
        @DisplayName("copies the delivery address from the request onto the order")
        void copiesDeliveryAddressOntoOrder() {
            //Given
            var captor = ArgumentCaptor.forClass(Order.class);

            //When
            orderService.createOrder(orderDto(), 42L);

            //Then
            then(orderRepo).should().save(captor.capture());
            var saved = captor.getValue();
            assertThat(saved.getFirstName()).isEqualTo("Jan");
            assertThat(saved.getLastName()).isEqualTo("Kowalski");
            assertThat(saved.getStreet()).isEqualTo("Kwiatowa 5");
            assertThat(saved.getZipCode()).isEqualTo("00-001");
            assertThat(saved.getCity()).isEqualTo("Warszawa");
            assertThat(saved.getEmail()).isEqualTo("jan.kowalski@example.com");
            assertThat(saved.getPhone()).isEqualTo("555111222");
        }

        @Test
        @DisplayName("marks a freshly placed order as NEW")
        void marksFreshOrderAsNew() {
            //Given
            var captor = ArgumentCaptor.forClass(Order.class);

            //When
            orderService.createOrder(orderDto(), 42L);

            //Then
            then(orderRepo).should().save(captor.capture());
            assertThat(captor.getValue().getOrderStatus()).isEqualTo(OrderStatus.NEW);
        }

        @Test
        @DisplayName("assigns the order to the authenticated caller, not to anything from the request body")
        void assignsOrderToAuthenticatedCaller() {
            //Given
            var captor = ArgumentCaptor.forClass(Order.class);

            //When
            orderService.createOrder(orderDto(), 42L);

            //Then
            then(orderRepo).should().save(captor.capture());
            assertThat(captor.getValue().getUserId()).isEqualTo(42L);
        }

        @Test
        @DisplayName("materialises one order row per basket line, linked to the persisted order")
        void materialisesOneRowPerBasketLine() {
            //Given
            var captor = ArgumentCaptor.forClass(OrderRow.class);

            //When
            orderService.createOrder(orderDto(), 42L);

            //Then
            then(orderRowRepo).should(times(3)).save(captor.capture());
            assertThat(captor.getAllValues())
                    .filteredOn(row -> row.getProductId() != null)
                    .extracting(OrderRow::getOrderId, OrderRow::getProductId, OrderRow::getQuantity, OrderRow::getPrice)
                    .containsExactlyInAnyOrder(
                            tuple(100L, 1L, 2L, new BigDecimal("10.00")),
                            tuple(100L, 2L, 3L, new BigDecimal("5.50")));
        }

        @Test
        @DisplayName("adds a dedicated order row carrying the shipment fee")
        void addsDedicatedShipmentRow() {
            //Given
            var captor = ArgumentCaptor.forClass(OrderRow.class);

            //When
            orderService.createOrder(orderDto(), 42L);

            //Then
            then(orderRowRepo).should(times(3)).save(captor.capture());
            assertThat(captor.getAllValues())
                    .filteredOn(row -> row.getShipmentId() != null)
                    .extracting(OrderRow::getOrderId, OrderRow::getShipmentId, OrderRow::getQuantity, OrderRow::getPrice)
                    .containsExactly(tuple(100L, 2L, 1L, new BigDecimal("15.00")));
        }

        @Test
        @DisplayName("empties the basket so the same items cannot be ordered twice")
        void emptiesTheBasket() {
            //Given
            var orderDto = orderDto();

            //When
            orderService.createOrder(orderDto, 42L);

            //Then
            then(basketItemRepo).should().deleteByBasketId(7L);
            then(basketRepo).should().deleteBasketById(7L);
        }

        @Test
        @DisplayName("sends the confirmation to the e-mail recorded on the order")
        void sendsConfirmationToOrderEmail() {
            //Given
            var orderDto = orderDto();

            //When
            orderService.createOrder(orderDto, 42L);

            //Then
            then(emailSender).should().sendEmail(eq("jan.kowalski@example.com"), eq("Order confirmation"), any());
        }

        @Test
        @DisplayName("tells the buyer the order id, the amount due and how to pay in the confirmation")
        void confirmationCarriesOrderIdAmountAndPaymentInstructions() {
            //Given
            var contentCaptor = ArgumentCaptor.forClass(String.class);

            //When
            orderService.createOrder(orderDto(), 42L);

            //Then
            then(emailSender).should().sendEmail(any(), any(), contentCaptor.capture());
            assertThat(contentCaptor.getValue())
                    .contains("100")
                    .contains("2026-07-19 12:30")
                    .contains("51.50")
                    .contains("Blik")
                    .contains("Pay within 24h");
        }
    }

    @Nested
    @DisplayName("refusing to place an order")
    class RefusingOrder {

        @Test
        @DisplayName("refuses an order for a basket that no longer exists and persists nothing")
        void refusesUnknownBasket() {
            //Given
            given(basketRepo.findById(7L)).willReturn(Optional.empty());

            //When //Then
            assertThatThrownBy(() -> orderService.createOrder(orderDto(), 42L))
                    .isInstanceOf(NoSuchElementException.class);

            then(orderRepo).should(never()).save(any());
            then(orderRowRepo).shouldHaveNoInteractions();
            then(basketItemRepo).shouldHaveNoInteractions();
            then(basketRepo).should(never()).deleteBasketById(any());
            then(emailClientService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("refuses an order with an unknown shipment method and leaves the basket intact")
        void refusesUnknownShipment() {
            //Given
            given(basketRepo.findById(7L)).willReturn(Optional.of(filledBasket()));
            given(shipmentRepo.findById(2L)).willReturn(Optional.empty());

            //When //Then
            assertThatThrownBy(() -> orderService.createOrder(orderDto(), 42L))
                    .isInstanceOf(NoSuchElementException.class);

            then(orderRepo).should(never()).save(any());
            then(orderRowRepo).shouldHaveNoInteractions();
            then(basketItemRepo).shouldHaveNoInteractions();
            then(basketRepo).should(never()).deleteBasketById(any());
            then(emailClientService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("refuses an order with an unknown payment method and leaves the basket intact")
        void refusesUnknownPayment() {
            //Given
            given(basketRepo.findById(7L)).willReturn(Optional.of(filledBasket()));
            given(shipmentRepo.findById(2L)).willReturn(Optional.of(shipment(2L, new BigDecimal("15.00"))));
            given(paymentRepo.findById(3L)).willReturn(Optional.empty());

            //When //Then
            assertThatThrownBy(() -> orderService.createOrder(orderDto(), 42L))
                    .isInstanceOf(NoSuchElementException.class);

            then(orderRepo).should(never()).save(any());
            then(orderRowRepo).shouldHaveNoInteractions();
            then(basketItemRepo).shouldHaveNoInteractions();
            then(basketRepo).should(never()).deleteBasketById(any());
            then(emailClientService).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("listing the orders of a user")
    class ListingUserOrders {

        @Test
        @DisplayName("flattens each order into id, date, status label and payment name")
        void flattensOrdersForTheCaller() {
            //Given
            var placedAt = LocalDateTime.of(2026, 7, 19, 12, 30);
            given(orderRepo.findAllByUserId(42L)).willReturn(List.of(
                    Order.builder()
                            .id(100L)
                            .placeDate(placedAt)
                            .orderStatus(OrderStatus.PAID)
                            .grossValue(new BigDecimal("51.50"))
                            .payment(payment(3L, "Blik", null))
                            .build()));

            //When
            var orders = orderService.getOrdersFromUser(42L);

            //Then
            assertThat(orders).containsExactly(
                    new OrderDtoForUser(100L, placedAt, "PAID", new BigDecimal("51.50"), "Blik"));
        }

        @Test
        @DisplayName("keeps every order of the user in the returned list")
        void keepsEveryOrderOfTheUser() {
            //Given
            given(orderRepo.findAllByUserId(42L)).willReturn(List.of(
                    order(100L, OrderStatus.COMPLETED),
                    order(101L, OrderStatus.CANCELLED)));

            //When
            var orders = orderService.getOrdersFromUser(42L);

            //Then
            assertThat(orders)
                    .extracting(OrderDtoForUser::id, OrderDtoForUser::status)
                    .containsExactly(tuple(100L, "COMPLETED"), tuple(101L, "CANCELLED"));
        }

        @Test
        @DisplayName("returns an empty list for a user who has never ordered")
        void returnsEmptyListForUserWithoutOrders() {
            //Given
            given(orderRepo.findAllByUserId(99L)).willReturn(List.of());

            //When
            var orders = orderService.getOrdersFromUser(99L);

            //Then
            assertThat(orders).isEmpty();
        }

        private Order order(Long id, OrderStatus status) {
            return Order.builder()
                    .id(id)
                    .placeDate(LocalDateTime.of(2026, 7, 19, 12, 30))
                    .orderStatus(status)
                    .grossValue(new BigDecimal("51.50"))
                    .payment(payment(3L, "Blik", null))
                    .build();
        }
    }

    private static OrderDto orderDto() {
        return new OrderDto(
                "Jan",
                "Kowalski",
                "Kwiatowa 5",
                "00-001",
                "Warszawa",
                "jan.kowalski@example.com",
                "555111222",
                7L,
                2L,
                3L);
    }

    private static Basket filledBasket() {
        return Basket.builder()
                .id(7L)
                .created(LocalDateTime.of(2026, 7, 19, 11, 0))
                .items(new ArrayList<>(List.of(
                        BasketItem.builder().product(product(1L, new BigDecimal("10.00"))).quantity(2L).build(),
                        BasketItem.builder().product(product(2L, new BigDecimal("5.50"))).quantity(3L).build())))
                .build();
    }

    private static Product product(Long id, BigDecimal price) {
        return Product.builder().id(id).price(price).build();
    }

    private static Shipment shipment(Long id, BigDecimal price) {
        var shipment = new Shipment();
        ReflectionTestUtils.setField(shipment, "id", id);
        ReflectionTestUtils.setField(shipment, "price", price);
        return shipment;
    }

    private static Payment payment(Long id, String name, String note) {
        var payment = new Payment();
        ReflectionTestUtils.setField(payment, "id", id);
        ReflectionTestUtils.setField(payment, "name", name);
        ReflectionTestUtils.setField(payment, "note", note);
        return payment;
    }
}
