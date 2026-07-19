package pl.dudios.shop.security.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import pl.dudios.shop.common.mail.EmailClientService;
import pl.dudios.shop.common.mail.EmailSender;
import pl.dudios.shop.security.user.model.AppUser;
import pl.dudios.shop.security.user.model.dto.ChangePassword;
import pl.dudios.shop.security.user.model.dto.EmailObject;
import pl.dudios.shop.security.user.repository.UserRepo;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * Procedura odzyskiwania hasła: wysyłka jednorazowego linku oraz jego realizacja.
 * <p>
 * Mockowane są wyłącznie granice systemu — repozytorium użytkowników (DB),
 * klient pocztowy wraz z senderem (I/O) i PasswordEncoder. Encja AppUser jest
 * prawdziwym obiektem domenowym, bo serwis działa w transakcji i zapisuje stan
 * przez dirty checking — obserwowalnym skutkiem jest więc stan encji, a nie
 * wywołanie save().
 * <p>
 * Adres bazowy pochodzi z @Value, którego w teście jednostkowym nie wstrzyknie
 * kontekst Springa — ustawiamy go wprost, żeby móc sprawdzić, że wysłany link
 * jest pełnym, klikalnym adresem, a nie samym hashem.
 */
@ExtendWith(MockitoExtension.class)
class LostPasswordServiceTest {

    private static final String BASE_ADDRESS = "https://shop.dudios.pl";
    private static final String EMAIL = "filip@dudios.pl";
    private static final String OLD_PASSWORD = "oldEncodedPassword";

    @Mock
    private UserRepo userRepo;
    @Mock
    private EmailClientService emailClientService;
    @Mock
    private EmailSender emailSender;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private LostPasswordService lostPasswordService;

    @BeforeEach
    void setBaseAddress() {
        ReflectionTestUtils.setField(lostPasswordService, "baseAddress", BASE_ADDRESS);
    }

    @Nested
    @DisplayName("requesting a lost-password link")
    class SendingLostPasswordLink {

        @Test
        @DisplayName("mails a link to the configured service address carrying exactly the hash stored on the user")
        void mailsLinkCarryingTheStoredHash() {
            //Given
            var user = newUser(7L, EMAIL);
            given(userRepo.findByUsername(EMAIL)).willReturn(Optional.of(user));
            given(emailClientService.getSender()).willReturn(emailSender);
            var content = ArgumentCaptor.forClass(String.class);

            //When
            lostPasswordService.sendLostPasswordLink(new EmailObject(EMAIL));

            //Then
            then(emailSender).should().sendEmail(eq(EMAIL), eq("Reset password"), content.capture());
            assertThat(content.getValue())
                    .contains(BASE_ADDRESS + "/lostPassword/" + user.getHash());
        }

        @Test
        @DisplayName("records when the hash was issued so the link can later expire")
        void recordsHashIssueTime() {
            //Given
            var user = newUser(7L, EMAIL);
            given(userRepo.findByUsername(EMAIL)).willReturn(Optional.of(user));
            given(emailClientService.getSender()).willReturn(emailSender);
            var before = LocalDateTime.now();

            //When
            lostPasswordService.sendLostPasswordLink(new EmailObject(EMAIL));

            //Then
            assertThat(user.getHashDate()).isBetween(before, LocalDateTime.now());
        }

        @Test
        @DisplayName("gives each account its own hash so one link cannot unlock another account")
        void givesEachAccountItsOwnHash() {
            //Given
            var first = newUser(7L, EMAIL);
            var second = newUser(8L, "anna@dudios.pl");
            given(userRepo.findByUsername(EMAIL)).willReturn(Optional.of(first));
            given(userRepo.findByUsername("anna@dudios.pl")).willReturn(Optional.of(second));
            given(emailClientService.getSender()).willReturn(emailSender);

            //When
            lostPasswordService.sendLostPasswordLink(new EmailObject(EMAIL));
            lostPasswordService.sendLostPasswordLink(new EmailObject("anna@dudios.pl"));

            //Then
            assertThat(first.getHash()).isNotEqualTo(second.getHash());
        }

        @Test
        @DisplayName("refuses an address that belongs to nobody and sends no message at all")
        void refusesUnknownAddress() {
            //Given
            given(userRepo.findByUsername("ghost@dudios.pl")).willReturn(Optional.empty());

            //When //Then
            assertThatThrownBy(() -> lostPasswordService.sendLostPasswordLink(new EmailObject("ghost@dudios.pl")))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found");

            then(emailClientService).shouldHaveNoInteractions();
            then(emailSender).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("redeeming a lost-password link")
    class ChangingPassword {

        @Test
        @DisplayName("refuses when the confirmation differs and never even looks the account up")
        void refusesMismatchedConfirmation() {
            //Given
            var request = new ChangePassword("newSecret", "newSecrat", "someHash");

            //When //Then
            assertThatThrownBy(() -> lostPasswordService.changePassword(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("takie same");

            then(userRepo).shouldHaveNoInteractions();
            then(passwordEncoder).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("refuses a hash that matches no account and never encodes anything")
        void refusesUnknownHash() {
            //Given
            given(userRepo.findByHash("forgedHash")).willReturn(Optional.empty());

            //When //Then
            assertThatThrownBy(() -> lostPasswordService.changePassword(
                    new ChangePassword("newSecret", "newSecret", "forgedHash")))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid link");

            then(passwordEncoder).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("stores the new password encoded, never in plain text")
        void storesNewPasswordEncoded() {
            //Given
            var user = userWithHash("validHash", LocalDateTime.now().minusMinutes(1));
            given(userRepo.findByHash("validHash")).willReturn(Optional.of(user));
            given(passwordEncoder.encode("newSecret")).willReturn("encodedNewSecret");

            //When
            lostPasswordService.changePassword(new ChangePassword("newSecret", "newSecret", "validHash"));

            //Then
            assertThat(user.getPassword()).isEqualTo("encodedNewSecret");
        }

        @Test
        @DisplayName("burns the hash after a successful reset so the same link cannot be used twice")
        void burnsHashAfterSuccessfulReset() {
            //Given
            var user = userWithHash("validHash", LocalDateTime.now().minusMinutes(1));
            given(userRepo.findByHash("validHash")).willReturn(Optional.of(user));
            given(passwordEncoder.encode("newSecret")).willReturn("encodedNewSecret");

            //When
            lostPasswordService.changePassword(new ChangePassword("newSecret", "newSecret", "validHash"));

            //Then
            assertThat(user.getHash()).isNull();
            assertThat(user.getHashDate()).isNull();
        }

        @Test
        @DisplayName("still accepts a link issued nine minutes ago — the window is ten minutes")
        void acceptsLinkJustInsideTheWindow() {
            //Given
            var user = userWithHash("validHash", LocalDateTime.now().minusMinutes(9));
            given(userRepo.findByHash("validHash")).willReturn(Optional.of(user));
            given(passwordEncoder.encode("newSecret")).willReturn("encodedNewSecret");

            //When
            lostPasswordService.changePassword(new ChangePassword("newSecret", "newSecret", "validHash"));

            //Then
            assertThat(user.getPassword()).isEqualTo("encodedNewSecret");
        }

        @Test
        @DisplayName("refuses a link that is exactly ten minutes old")
        void refusesLinkExactlyAtTheWindowEdge() {
            //Given
            var user = userWithHash("staleHash", LocalDateTime.now().minusMinutes(10));
            given(userRepo.findByHash("staleHash")).willReturn(Optional.of(user));

            //When //Then
            assertThatThrownBy(() -> lostPasswordService.changePassword(
                    new ChangePassword("newSecret", "newSecret", "staleHash")))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("expired");
        }

        @Test
        @DisplayName("refuses an expired link and leaves the old password untouched")
        void refusesExpiredLinkAndKeepsOldPassword() {
            //Given
            var user = userWithHash("staleHash", LocalDateTime.now().minusMinutes(30));
            given(userRepo.findByHash("staleHash")).willReturn(Optional.of(user));

            //When //Then
            assertThatThrownBy(() -> lostPasswordService.changePassword(
                    new ChangePassword("newSecret", "newSecret", "staleHash")))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("expired");

            assertThat(user.getPassword()).isEqualTo(OLD_PASSWORD);
            then(passwordEncoder).shouldHaveNoInteractions();
        }
    }

    private static AppUser newUser(Long id, String username) {
        return AppUser.builder()
                .id(id)
                .username(username)
                .password(OLD_PASSWORD)
                .enabled(true)
                .build();
    }

    private static AppUser userWithHash(String hash, LocalDateTime issuedAt) {
        var user = newUser(7L, EMAIL);
        user.setHash(hash);
        user.setHashDate(issuedAt);
        return user;
    }
}
