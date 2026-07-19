package pl.dudios.shop.common.mail;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Zastępcza implementacja EmailSender używana tam, gdzie nie ma (albo nie chcemy)
 * transportu SMTP — środowisko deweloperskie, demo, testy manualne. Nie ma tu
 * żadnej granicy systemu do zamockowania: klasa nie ma współpracowników, więc
 * testowana jest prawdziwa instancja.
 * <p>
 * Wartość biznesowa tej klasy sprowadza się do dwóch rzeczy i tylko te są sprawdzane:
 * (1) jest bezpiecznym zamiennikiem prawdziwego sendera — proces biznesowy, w środku
 * którego leci mail (potwierdzenie zamówienia, reset hasła), nie wywraca się przez brak
 * poczty; (2) wiadomość, która NIE poszła, daje się odczytać z logu, bo inaczej
 * deweloper nie wyciągnie np. linku resetującego hasło i atrapa jest bezużyteczna.
 * <p>
 * Log jest tutaj jedynym obserwowalnym efektem działania klasy, dlatego asercje idą
 * po przechwyconych zdarzeniach logback (ListAppender), a nie po dosłownym brzmieniu
 * komunikatów — sprawdzamy, że adresat, temat i treść w ogóle są w logu obecne.
 */
class FakeEmailServiceTest {

    private static final String RECIPIENT = "anna.kowalska@example.com";
    private static final String SUBJECT = "Reset hasła";

    private final FakeEmailService fakeEmailService = new FakeEmailService();

    private Logger serviceLogger;
    private Level originalLevel;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void captureServiceLog() {
        serviceLogger = (Logger) LoggerFactory.getLogger(FakeEmailService.class);
        originalLevel = serviceLogger.getLevel();
        serviceLogger.setLevel(Level.TRACE);

        logAppender = new ListAppender<>();
        logAppender.start();
        serviceLogger.addAppender(logAppender);
    }

    @AfterEach
    void releaseServiceLog() {
        serviceLogger.detachAppender(logAppender);
        logAppender.stop();
        serviceLogger.setLevel(originalLevel);
    }

    @Nested
    @DisplayName("standing in for a real mail transport")
    class StandingInForMailTransport {

        @Test
        @DisplayName("sends without any mail infrastructure so the business flow around it completes")
        void sendsWithoutMailInfrastructure() {
            //Given
            var content = resetPasswordContent();

            //When //Then
            assertThatCode(() -> fakeEmailService.sendEmail(RECIPIENT, SUBJECT, content))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("leaving the undelivered message behind in the log")
    class LeavingMessageInLog {

        @Test
        @DisplayName("records recipient, subject and body so the message can be read back without SMTP")
        void recordsWholeMessage() {
            //Given
            var content = resetPasswordContent();

            //When
            fakeEmailService.sendEmail(RECIPIENT, SUBJECT, content);

            //Then
            assertThat(loggedOutput())
                    .contains(RECIPIENT)
                    .contains(SUBJECT)
                    .contains(content);
        }

        @Test
        @DisplayName("keeps consecutive messages apart so a later send does not hide an earlier one")
        void keepsConsecutiveMessagesApart() {
            //Given
            var firstContent = "Zamówienie 42 zostało przyjęte.";
            var secondContent = "Zamówienie 77 zostało przyjęte.";

            //When
            fakeEmailService.sendEmail("first@example.com", "Potwierdzenie zamówienia", firstContent);
            fakeEmailService.sendEmail("second@example.com", "Potwierdzenie zamówienia", secondContent);

            //Then
            assertThat(loggedOutput())
                    .contains("first@example.com")
                    .contains(firstContent)
                    .contains("second@example.com")
                    .contains(secondContent);
        }

        @Test
        @DisplayName("writes at INFO so the message is visible under the shop's default log level")
        void writesAtInfoLevel() {
            //Given
            var content = resetPasswordContent();

            //When
            fakeEmailService.sendEmail(RECIPIENT, SUBJECT, content);

            //Then
            assertThat(logAppender.list)
                    .extracting(ILoggingEvent::getLevel)
                    .containsOnly(Level.INFO);
        }
    }

    private static String resetPasswordContent() {
        return """
                Cześć Anno,
                zresetuj hasło: https://dudios.pl/reset?token=abc-123
                Link wygasa po 24 godzinach.""";
    }

    private String loggedOutput() {
        return logAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.joining("\n"));
    }
}
