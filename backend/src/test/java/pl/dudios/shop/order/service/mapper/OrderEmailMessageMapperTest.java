package pl.dudios.shop.order.service.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import pl.dudios.shop.order.model.Order;
import pl.dudios.shop.order.model.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reguły budowania treści maila z potwierdzeniem złożonego zamówienia.
 * OrderEmailMessageMapper to klasa czysto statyczna bez współpracowników — nie ma
 * tu żadnej granicy systemu do zamockowania, więc Order i Payment są prawdziwymi
 * encjami domenowymi. Payment nie ma ani buildera, ani setterów, ani konstruktora
 * z argumentami, dlatego jedyną drogą do zbudowania realnej instancji jest
 * ReflectionTestUtils (tak samo jak w OrderMapperTest).
 * Testy pilnują tego, co klient przeczyta w mailu: numeru zamówienia, daty
 * złożenia, kwoty wraz z walutą, nazwy płatności oraz instrukcji do przelewu.
 */
class OrderEmailMessageMapperTest {

    private static final Long ORDER_ID = 4242L;

    @Nested
    @DisplayName("what the customer can read in the confirmation email")
    class ConfirmationContent {

        @Test
        @DisplayName("identifies the order by its id so the customer can quote it in a complaint")
        void identifiesTheOrderById() {
            //Given
            var order = placedOrder(payment("Karta"));

            //When
            var message = OrderEmailMessageMapper.createEmailMessage(order);

            //Then
            assertThat(message).contains(String.valueOf(ORDER_ID));
        }

        @Test
        @DisplayName("states the amount due together with the currency, not a bare number")
        void statesAmountDueWithCurrency() {
            //Given
            var order = placedOrder(payment("Karta"));

            //When
            var message = OrderEmailMessageMapper.createEmailMessage(order);

            //Then
            assertThat(message).contains("249.99 PLN");
        }

        @Test
        @DisplayName("names the payment method the customer picked at checkout")
        void namesTheChosenPaymentMethod() {
            //Given
            var order = placedOrder(payment("Przelew bankowy"));

            //When
            var message = OrderEmailMessageMapper.createEmailMessage(order);

            //Then
            assertThat(message).contains("Przelew bankowy");
        }
    }

    @Nested
    @DisplayName("the date the order was placed")
    class PlaceDateFormatting {

        @Test
        @DisplayName("shows the date down to the minute and drops the seconds — a customer does not need them")
        void showsDateDownToTheMinuteWithoutSeconds() {
            //Given
            var order = placedOrder(payment("Karta"));

            //When
            var message = OrderEmailMessageMapper.createEmailMessage(order);

            //Then
            assertThat(message).contains("2026-03-01 09:05");
            assertThat(message).doesNotContain("09:05:45");
        }
    }

    @Nested
    @DisplayName("payment instructions attached to the chosen payment method")
    class PaymentNote {

        @Test
        @DisplayName("passes on the payment note so the customer knows where to transfer the money")
        void passesOnThePaymentNote() {
            //Given
            var order = placedOrder(paymentWithNote("Przelew bankowy", "Account number: PL61 1090 1014 0000"));

            //When
            var message = OrderEmailMessageMapper.createEmailMessage(order);

            //Then
            assertThat(message).contains("Account number: PL61 1090 1014 0000");
        }

        @Test
        @DisplayName("says nothing about instructions when the payment method has none — no stray \"null\" in the email")
        void saysNothingWhenPaymentHasNoNote() {
            //Given
            var order = placedOrder(payment("Karta"));

            //When
            var message = OrderEmailMessageMapper.createEmailMessage(order);

            //Then
            assertThat(message).doesNotContain("null");
        }
    }

    private static Order placedOrder(Payment payment) {
        return Order.builder()
                .id(ORDER_ID)
                .placeDate(LocalDateTime.of(2026, 3, 1, 9, 5, 45))
                .grossValue(new BigDecimal("249.99"))
                .payment(payment)
                .build();
    }

    private static Payment payment(String name) {
        var payment = new Payment();
        ReflectionTestUtils.setField(payment, "name", name);
        return payment;
    }

    private static Payment paymentWithNote(String name, String note) {
        var payment = payment(name);
        ReflectionTestUtils.setField(payment, "note", note);
        return payment;
    }
}
