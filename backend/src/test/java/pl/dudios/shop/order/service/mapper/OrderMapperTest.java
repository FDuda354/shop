package pl.dudios.shop.order.service.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import pl.dudios.shop.common.model.Basket;
import pl.dudios.shop.common.model.BasketItem;
import pl.dudios.shop.common.model.OrderStatus;
import pl.dudios.shop.common.model.Product;
import pl.dudios.shop.order.model.Order;
import pl.dudios.shop.order.model.Payment;
import pl.dudios.shop.order.model.Shipment;
import pl.dudios.shop.order.model.dto.OrderDto;
import pl.dudios.shop.order.model.dto.OrderDtoForUser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reguły przeliczania koszyka na zamówienie i na wiersze zamówienia.
 * OrderMapper to klasa czysto statyczna bez współpracowników — nie ma tu żadnej
 * granicy systemu do zamockowania, więc wszystkie obiekty (Basket, BasketItem,
 * Product, Order, Shipment, Payment) są prawdziwymi encjami domenowymi.
 * Shipment i Payment nie mają ani buildera, ani setterów, ani konstruktora
 * z argumentami, dlatego jedyną drogą do zbudowania realnej instancji jest
 * ReflectionTestUtils.
 * Testy pilnują tego, co widzi wołający: kwoty do zapłaty, statusu zamówienia,
 * danych dostawy i kształtu wierszy zapisywanych do bazy.
 */
class OrderMapperTest {

    @Nested
    @DisplayName("turning a checkout request into a new order")
    class CreatingNewOrder {

        @Test
        @DisplayName("carries the customer's delivery address and contact details onto the order")
        void carriesDeliveryAddressAndContactDetails() {
            //Given
            var dto = orderDto();

            //When
            var order = OrderMapper.createNewOrder(dto, basketWith(), shipment(1L, "0.00"), payment("Karta"), 1L);

            //Then
            assertThat(order.getFirstName()).isEqualTo("Filip");
            assertThat(order.getLastName()).isEqualTo("Duda");
            assertThat(order.getStreet()).isEqualTo("Kwiatowa 5");
            assertThat(order.getZipCode()).isEqualTo("00-950");
            assertThat(order.getCity()).isEqualTo("Warszawa");
            assertThat(order.getEmail()).isEqualTo("filip@example.com");
            assertThat(order.getPhone()).isEqualTo("600100200");
        }

        @Test
        @DisplayName("opens every order as NEW — nothing is paid or shipped at checkout time")
        void opensEveryOrderAsNew() {
            //Given
            var dto = orderDto();

            //When
            var order = OrderMapper.createNewOrder(dto, basketWith(), shipment(1L, "0.00"), payment("Karta"), 1L);

            //Then
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.NEW);
        }

        @Test
        @DisplayName("charges the customer for every basket item plus the shipment price")
        void chargesForEveryItemPlusShipment() {
            //Given
            var basket = basketWith(item(1L, "10.00", 3), item(2L, "5.50", 2));

            //When
            var order = OrderMapper.createNewOrder(orderDto(), basket, shipment(1L, "15.49"), payment("Karta"), 1L);

            //Then
            assertThat(order.getGrossValue()).isEqualByComparingTo("56.49");
        }

        @Test
        @DisplayName("charges the shipment price alone when the basket holds no products")
        void chargesShipmentAloneForEmptyBasket() {
            //Given
            var basket = basketWith();

            //When
            var order = OrderMapper.createNewOrder(orderDto(), basket, shipment(1L, "15.49"), payment("Karta"), 1L);

            //Then
            assertThat(order.getGrossValue()).isEqualByComparingTo("15.49");
        }

        @Test
        @DisplayName("binds the order to the authenticated user so it shows up in their history")
        void bindsOrderToAuthenticatedUser() {
            //Given
            var dto = orderDto();

            //When
            var order = OrderMapper.createNewOrder(dto, basketWith(), shipment(1L, "0.00"), payment("Karta"), 42L);

            //Then
            assertThat(order.getUserId()).isEqualTo(42L);
        }

        @Test
        @DisplayName("accepts a guest checkout — an order without a user is still a valid order")
        void acceptsGuestCheckout() {
            //Given
            var dto = orderDto();

            //When
            var order = OrderMapper.createNewOrder(dto, basketWith(), shipment(1L, "0.00"), payment("Karta"), null);

            //Then
            assertThat(order.getUserId()).isNull();
        }

        @Test
        @DisplayName("attaches the payment method the customer picked")
        void attachesChosenPaymentMethod() {
            //Given
            var chosen = payment("Przelew");

            //When
            var order = OrderMapper.createNewOrder(orderDto(), basketWith(), shipment(1L, "0.00"), chosen, 1L);

            //Then
            assertThat(order.getPayment()).isSameAs(chosen);
        }

        @Test
        @DisplayName("stamps the order with the moment it was placed")
        void stampsOrderWithPlacementMoment() {
            //Given
            var before = LocalDateTime.now();

            //When
            var order = OrderMapper.createNewOrder(orderDto(), basketWith(), shipment(1L, "0.00"), payment("Karta"), 1L);

            //Then
            assertThat(order.getPlaceDate()).isBetween(before, LocalDateTime.now());
        }
    }

    @Nested
    @DisplayName("summarising a placed order back to the client")
    class BuildingOrderSummary {

        @Test
        @DisplayName("reports the id, placement date, status, value and payment of the persisted order")
        void reportsPersistedOrderDetails() {
            //Given
            var paid = payment("Karta");
            var order = placedOrder(7L, OrderStatus.NEW, "56.49", paid);

            //When
            var summary = OrderMapper.createOrderSummary(order);

            //Then
            assertThat(summary.id()).isEqualTo(7L);
            assertThat(summary.placeDate()).isEqualTo(LocalDateTime.of(2026, 3, 1, 12, 0));
            assertThat(summary.status()).isEqualTo(OrderStatus.NEW);
            assertThat(summary.grossValue()).isEqualByComparingTo("56.49");
            assertThat(summary.payment()).isSameAs(paid);
        }
    }

    @Nested
    @DisplayName("billing the shipment as an order row")
    class MappingShipmentRow {

        @Test
        @DisplayName("bills the shipment exactly once at its own price")
        void billsShipmentOnceAtItsOwnPrice() {
            //Given
            var shipment = shipment(3L, "15.49");

            //When
            var row = OrderMapper.mapToOrderRow(99L, shipment);

            //Then
            assertThat(row.getOrderId()).isEqualTo(99L);
            assertThat(row.getShipmentId()).isEqualTo(3L);
            assertThat(row.getQuantity()).isEqualTo(1L);
            assertThat(row.getPrice()).isEqualByComparingTo("15.49");
        }

        @Test
        @DisplayName("leaves the shipment row without a product so it is never counted as goods")
        void leavesShipmentRowWithoutProduct() {
            //Given
            var shipment = shipment(3L, "15.49");

            //When
            var row = OrderMapper.mapToOrderRow(99L, shipment);

            //Then
            assertThat(row.getProductId()).isNull();
        }
    }

    @Nested
    @DisplayName("billing basket items as order rows")
    class MappingProductRow {

        @Test
        @DisplayName("freezes the product's current price and the ordered quantity on the row")
        void freezesPriceAndQuantity() {
            //Given
            var item = item(77L, "12.34", 4);

            //When
            var row = OrderMapper.mapToOrderRowWithQuantity(99L, item);

            //Then
            assertThat(row.getOrderId()).isEqualTo(99L);
            assertThat(row.getProductId()).isEqualTo(77L);
            assertThat(row.getPrice()).isEqualByComparingTo("12.34");
            assertThat(row.getQuantity()).isEqualTo(4L);
        }

        @Test
        @DisplayName("leaves the product row without a shipment so it is never counted as delivery cost")
        void leavesProductRowWithoutShipment() {
            //Given
            var item = item(77L, "12.34", 4);

            //When
            var row = OrderMapper.mapToOrderRowWithQuantity(99L, item);

            //Then
            assertThat(row.getShipmentId()).isNull();
        }
    }

    @Nested
    @DisplayName("listing the orders a user has placed")
    class ListingUserOrders {

        @Test
        @DisplayName("returns one entry per order, keeping the order the repository delivered them in")
        void returnsOneEntryPerOrderInRepositoryOrder() {
            //Given
            var orders = List.of(
                    placedOrder(7L, OrderStatus.NEW, "56.49", payment("Karta")),
                    placedOrder(8L, OrderStatus.COMPLETED, "10.00", payment("Przelew")));

            //When
            var dtos = OrderMapper.createOrderListDtoForUser(orders);

            //Then
            assertThat(dtos).extracting(OrderDtoForUser::id).containsExactly(7L, 8L);
        }

        @Test
        @DisplayName("presents each order the way a customer reads it — date, value, status label and payment name")
        void presentsOrderInCustomerReadableForm() {
            //Given
            var orders = List.of(placedOrder(7L, OrderStatus.PAID, "56.49", payment("Karta")));

            //When
            var dtos = OrderMapper.createOrderListDtoForUser(orders);

            //Then
            var dto = dtos.getFirst();
            assertThat(dto.id()).isEqualTo(7L);
            assertThat(dto.placeDate()).isEqualTo(LocalDateTime.of(2026, 3, 1, 12, 0));
            assertThat(dto.status()).isEqualTo("PAID");
            assertThat(dto.grossValue()).isEqualByComparingTo("56.49");
            assertThat(dto.paymentName()).isEqualTo("Karta");
        }

        @Test
        @DisplayName("returns an empty history for a user who has never ordered anything")
        void returnsEmptyHistoryForUserWithoutOrders() {
            //Given
            var orders = List.<Order>of();

            //When
            var dtos = OrderMapper.createOrderListDtoForUser(orders);

            //Then
            assertThat(dtos).isEmpty();
        }
    }

    private static OrderDto orderDto() {
        return new OrderDto(
                "Filip",
                "Duda",
                "Kwiatowa 5",
                "00-950",
                "Warszawa",
                "filip@example.com",
                "600100200",
                1L,
                2L,
                3L);
    }

    private static Basket basketWith(BasketItem... items) {
        return Basket.builder()
                .items(List.of(items))
                .build();
    }

    private static BasketItem item(Long productId, String price, long quantity) {
        return BasketItem.builder()
                .product(Product.builder().id(productId).price(new BigDecimal(price)).build())
                .quantity(quantity)
                .build();
    }

    private static Order placedOrder(Long id, OrderStatus status, String grossValue, Payment payment) {
        return Order.builder()
                .id(id)
                .placeDate(LocalDateTime.of(2026, 3, 1, 12, 0))
                .orderStatus(status)
                .grossValue(new BigDecimal(grossValue))
                .payment(payment)
                .build();
    }

    private static Shipment shipment(Long id, String price) {
        var shipment = new Shipment();
        ReflectionTestUtils.setField(shipment, "id", id);
        ReflectionTestUtils.setField(shipment, "price", new BigDecimal(price));
        return shipment;
    }

    private static Payment payment(String name) {
        var payment = new Payment();
        ReflectionTestUtils.setField(payment, "name", name);
        return payment;
    }
}
