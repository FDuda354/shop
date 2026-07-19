package pl.dudios.shop.common.mail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;

/**
 * Wysyłka maili transakcyjnych sklepu (potwierdzenie zamówienia, reset hasła,
 * zmiana statusu). Mockowana jest wyłącznie granica systemu — JavaMailSender,
 * czyli faktyczny transport SMTP. Testy pilnują tego, co obserwuje wołający:
 * jaka wiadomość trafia do transportu (adresat, temat, treść, tożsamość
 * nadawcy sklepu) oraz tego, że awaria wysyłki nie wywraca procesu biznesowego,
 * w środku którego mail jest wysyłany.
 */
@ExtendWith(MockitoExtension.class)
class EmailSimpleServiceTest {

    private static final String SHOP_MAILBOX = "postmaster@dudios.pl";
    private static final String SHOP_REPLY_MAILBOX = "filip@dudios.pl";

    @Mock
    private JavaMailSender mailSender;
    @InjectMocks
    private EmailSimpleService emailSimpleService;

    @Nested
    @DisplayName("handing a message over to the mail transport")
    class DeliveringMessage {

        @Test
        @DisplayName("delivers the recipient, subject and body exactly as the caller provided them")
        void deliversCallerContentUnchanged() {
            //Given
            var content = "Your order 42 has been confirmed.";

            //When
            emailSimpleService.sendEmail("customer@example.com", "Order confirmation", content);

            //Then
            var sent = sentMessages(1).getFirst();
            assertThat(sent.getTo()).containsExactly("customer@example.com");
            assertThat(sent.getSubject()).isEqualTo("Order confirmation");
            assertThat(sent.getText()).isEqualTo(content);
        }

        @Test
        @DisplayName("stamps the shop mailbox as sender and routes replies to the shop reply address")
        void stampsShopSenderIdentity() {
            //Given
            var recipient = "customer@example.com";

            //When
            emailSimpleService.sendEmail(recipient, "Reset password", "Follow the link.");

            //Then
            var sent = sentMessages(1).getFirst();
            assertThat(sent.getFrom()).contains(SHOP_MAILBOX);
            assertThat(sent.getReplyTo()).contains(SHOP_REPLY_MAILBOX);
        }

        @Test
        @DisplayName("keeps each recipient's message separate so two customers never share one body")
        void keepsMessagesPerRecipientSeparate() {
            //Given
            var firstBody = "Your order 42 has been confirmed.";
            var secondBody = "Your order 77 has been confirmed.";

            //When
            emailSimpleService.sendEmail("first@example.com", "Order confirmation", firstBody);
            emailSimpleService.sendEmail("second@example.com", "Order confirmation", secondBody);

            //Then
            var sent = sentMessages(2);
            assertThat(sent.getFirst().getTo()).containsExactly("first@example.com");
            assertThat(sent.getFirst().getText()).isEqualTo(firstBody);
            assertThat(sent.getLast().getTo()).containsExactly("second@example.com");
            assertThat(sent.getLast().getText()).isEqualTo(secondBody);
        }
    }

    @Nested
    @DisplayName("when the mail transport fails")
    class TransportFailure {

        @Test
        @DisplayName("swallows the delivery failure so the surrounding business flow is not rolled back")
        void swallowsDeliveryFailure() {
            //Given
            willThrow(new MailSendException("SMTP host unreachable"))
                    .given(mailSender).send(any(SimpleMailMessage.class));

            //When //Then
            assertThatCode(() -> emailSimpleService.sendEmail(
                    "customer@example.com", "Order confirmation", "Your order 42 has been confirmed."))
                    .doesNotThrowAnyException();
        }
    }

    private List<SimpleMailMessage> sentMessages(int expectedCount) {
        var captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        then(mailSender).should(times(expectedCount)).send(captor.capture());
        return captor.getAllValues();
    }
}
