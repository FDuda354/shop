package pl.dudios.shop.admin.order.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dudios.shop.admin.order.model.AdminOrder;
import pl.dudios.shop.common.mail.EmailClientService;
import pl.dudios.shop.common.mail.EmailSender;
import pl.dudios.shop.common.model.OrderStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * Reguły powiadamiania klienta o zmianie statusu zamówienia.
 * Mockowana jest wyłącznie granica wysyłki maili (EmailClientService i zwracany
 * przez niego EmailSender); AdminOrder oraz OrderStatus to prawdziwe obiekty
 * domenowe. Testy pilnują tego, co obserwuje klient — czy mail w ogóle poszedł,
 * na jaki adres, z jakim tematem i treścią — a nie tego, jak serwis rozgałęzia
 * warunki w środku.
 */
@ExtendWith(MockitoExtension.class)
class AdminOrderEmailMessageTest {

    private static final Long ORDER_ID = 42L;
    private static final String CLIENT_EMAIL = "jan.kowalski@example.com";

    @Mock
    private EmailClientService emailClientService;
    @Mock
    private EmailSender emailSender;
    @InjectMocks
    private AdminOrderEmailMessage adminOrderEmailMessage;

    @Nested
    @DisplayName("statuses the client must be told about")
    class NotifyingClient {

        @BeforeEach
        void wireSender() {
            given(emailClientService.getSender()).willReturn(emailSender);
        }

        @ParameterizedTest
        @EnumSource(value = OrderStatus.class, names = {"PROCESSING", "WAITING_FOR_DELIVERY", "COMPLETED", "REFUND"})
        @DisplayName("sends exactly one notification whose subject names the order and the status it moved to")
        void sendsOneNotificationNamingOrderAndStatus(OrderStatus newStatus) {
            //Given
            var order = newOrder();

            //When
            adminOrderEmailMessage.notifyClient(newStatus, order);

            //Then
            var sent = captureSentEmail();
            assertThat(sent.subject()).contains(String.valueOf(ORDER_ID), newStatus.getValue());
        }

        @Test
        @DisplayName("delivers the notification to the address stored on the order")
        void deliversToAddressOnTheOrder() {
            //Given
            var order = newOrder();

            //When
            adminOrderEmailMessage.notifyClient(OrderStatus.COMPLETED, order);

            //Then
            var sent = captureSentEmail();
            assertThat(sent.to()).isEqualTo(CLIENT_EMAIL);
        }

        @Test
        @DisplayName("sends the personalised greeting as the body so the client reads who and what it is about")
        void sendsPersonalisedGreetingAsBody() {
            //Given
            var order = newOrder();

            //When
            adminOrderEmailMessage.notifyClient(OrderStatus.REFUND, order);

            //Then
            var sent = captureSentEmail();
            assertThat(sent.content()).contains("Jan", "Kowalski", "refund");
        }
    }

    @Nested
    @DisplayName("statuses the client is deliberately not told about")
    class SilentStatuses {

        @ParameterizedTest
        @EnumSource(value = OrderStatus.class, names = {"NEW", "PAID", "CANCELLED"})
        @DisplayName("sends nothing and never even reaches for a mail sender")
        void sendsNothingForSilentStatuses(OrderStatus newStatus) {
            //Given
            var order = newOrder();

            //When
            adminOrderEmailMessage.notifyClient(newStatus, order);

            //Then
            then(emailClientService).shouldHaveNoInteractions();
            then(emailSender).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("the message the client receives")
    class MessageContent {

        @Test
        @DisplayName("greets the client by full name and identifies the order by its id")
        void greetsByFullNameAndIdentifiesOrder() {
            //Given
            var order = newOrder();

            //When
            var message = AdminOrderEmailMessage.createMessage(order, OrderStatus.COMPLETED);

            //Then
            assertThat(message).contains("Jan", "Kowalski", String.valueOf(ORDER_ID));
        }

        @Test
        @DisplayName("spells the new status in lower case so it reads as a sentence, not as an enum constant")
        void spellsStatusInLowerCase() {
            //Given
            var order = newOrder();

            //When
            var message = AdminOrderEmailMessage.createMessage(order, OrderStatus.WAITING_FOR_DELIVERY);

            //Then
            assertThat(message).contains("waiting_for_delivery");
            assertThat(message).doesNotContain("WAITING_FOR_DELIVERY");
        }
    }

    private SentEmail captureSentEmail() {
        var to = ArgumentCaptor.forClass(String.class);
        var subject = ArgumentCaptor.forClass(String.class);
        var content = ArgumentCaptor.forClass(String.class);
        then(emailSender).should().sendEmail(to.capture(), subject.capture(), content.capture());
        return new SentEmail(to.getValue(), subject.getValue(), content.getValue());
    }

    private static AdminOrder newOrder() {
        var order = new AdminOrder();
        order.setId(ORDER_ID);
        order.setFirstName("Jan");
        order.setLastName("Kowalski");
        order.setEmail(CLIENT_EMAIL);
        return order;
    }

    private record SentEmail(String to, String subject, String content) {
    }
}
