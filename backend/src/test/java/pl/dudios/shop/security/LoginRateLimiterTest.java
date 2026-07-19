package pl.dudios.shop.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Reguły ochrony logowania przed brute-force: ile nieudanych prób wolno oddać
 * w oknie czasowym, kogo blokada dotyczy i co ją zdejmuje. Klasa nie ma żadnych
 * współpracowników — cały stan trzyma w pamięci procesu — więc nic nie jest
 * mockowane, a testy patrzą wyłącznie na to, co widzi wołający (AuthService):
 * czy kolejna próba logowania jest dopuszczona, czy odrzucona.
 * Okno 60 sekund liczone jest po Instant.now() wprost w środku klasy, więc
 * wygasanie starych porażek nie da się sprawdzić deterministycznie i celowo
 * nie jest tu testowane.
 */
class LoginRateLimiterTest {

    private LoginRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new LoginRateLimiter();
    }

    @Nested
    @DisplayName("a username that has never failed")
    class UsernameWithoutFailures {

        @Test
        @DisplayName("lets an unknown username try to log in")
        void letsUnknownUsernameIn() {
            //Given
            var username = "filip";

            //When
            var allowed = rateLimiter.isAllowed(username);

            //Then
            assertThat(allowed).isTrue();
        }

        @Test
        @DisplayName("lets a missing username through instead of blowing up on it")
        void letsMissingUsernameThrough() {
            //Given //When
            var allowed = rateLimiter.isAllowed(null);

            //Then
            assertThat(allowed).isTrue();
        }
    }

    @Nested
    @DisplayName("counting failed login attempts")
    class CountingFailures {

        @Test
        @DisplayName("still lets the user in after 14 failures — the limit is not reached yet")
        void allowsUserBelowTheLimit() {
            //Given
            recordFailures("filip", 14);

            //When
            var allowed = rateLimiter.isAllowed("filip");

            //Then
            assertThat(allowed).isTrue();
        }

        @Test
        @DisplayName("locks the user out on the 15th failure inside the window")
        void blocksUserAtTheLimit() {
            //Given
            recordFailures("filip", 15);

            //When
            var allowed = rateLimiter.isAllowed("filip");

            //Then
            assertThat(allowed).isFalse();
        }

        @Test
        @DisplayName("keeps the user locked out while failures keep piling up")
        void keepsUserBlockedBeyondTheLimit() {
            //Given
            recordFailures("filip", 40);

            //When
            var allowed = rateLimiter.isAllowed("filip");

            //Then
            assertThat(allowed).isFalse();
        }

        @Test
        @DisplayName("treats FILIP and filip as the same account, so changing case does not dodge the block")
        void countsFailuresCaseInsensitively() {
            //Given
            recordFailures("FILIP", 15);

            //When
            var allowed = rateLimiter.isAllowed("filip");

            //Then
            assertThat(allowed).isFalse();
        }

        @Test
        @DisplayName("ignores surrounding whitespace, so padding the username does not dodge the block")
        void countsFailuresIgnoringWhitespace() {
            //Given
            recordFailures("  filip  ", 15);

            //When
            var allowed = rateLimiter.isAllowed("filip");

            //Then
            assertThat(allowed).isFalse();
        }

        @Test
        @DisplayName("locks out only the account that failed and leaves other users able to log in")
        void blocksOnlyTheFailingAccount() {
            //Given
            recordFailures("filip", 15);

            //When
            var otherUserAllowed = rateLimiter.isAllowed("anna");

            //Then
            assertThat(otherUserAllowed).isTrue();
            assertThat(rateLimiter.isAllowed("filip")).isFalse();
        }
    }

    @Nested
    @DisplayName("clearing the failure counter of one user")
    class ResettingSingleUser {

        @Test
        @DisplayName("lets a locked out user log in again once a successful login clears the counter")
        void unblocksUserAfterReset() {
            //Given
            recordFailures("filip", 15);

            //When
            rateLimiter.reset("filip");

            //Then
            assertThat(rateLimiter.isAllowed("filip")).isTrue();
        }

        @Test
        @DisplayName("clears the counter no matter how the username was typed")
        void unblocksUserRegardlessOfSpelling() {
            //Given
            recordFailures("filip", 15);

            //When
            rateLimiter.reset("  FILIP ");

            //Then
            assertThat(rateLimiter.isAllowed("filip")).isTrue();
        }

        @Test
        @DisplayName("clears the counter from scratch — the freed user gets the full budget again")
        void restartsCountingAfterReset() {
            //Given
            recordFailures("filip", 15);
            rateLimiter.reset("filip");

            //When
            recordFailures("filip", 14);

            //Then
            assertThat(rateLimiter.isAllowed("filip")).isTrue();
        }

        @Test
        @DisplayName("clearing one user does not unlock anybody else")
        void doesNotUnblockOtherUsers() {
            //Given
            recordFailures("filip", 15);
            recordFailures("anna", 15);

            //When
            rateLimiter.reset("filip");

            //Then
            assertThat(rateLimiter.isAllowed("filip")).isTrue();
            assertThat(rateLimiter.isAllowed("anna")).isFalse();
        }

        @Test
        @DisplayName("clearing a user who never failed is harmless — every successful login does it")
        void resettingCleanUserIsHarmless() {
            //Given
            var username = "ghost";

            //When //Then
            assertThatCode(() -> rateLimiter.reset(username)).doesNotThrowAnyException();
            assertThat(rateLimiter.isAllowed(username)).isTrue();
        }
    }

    @Nested
    @DisplayName("clearing the failure counters of everybody")
    class ResettingEveryUser {

        @Test
        @DisplayName("unlocks every locked out user at once")
        void unblocksAllUsers() {
            //Given
            recordFailures("filip", 15);
            recordFailures("anna", 15);

            //When
            rateLimiter.resetAll();

            //Then
            assertThat(rateLimiter.isAllowed("filip")).isTrue();
            assertThat(rateLimiter.isAllowed("anna")).isTrue();
        }

        @Test
        @DisplayName("wipes the counters instead of only lifting the block — counting starts from zero")
        void restartsCountingAfterGlobalReset() {
            //Given
            recordFailures("filip", 15);
            rateLimiter.resetAll();

            //When
            recordFailures("filip", 14);

            //Then
            assertThat(rateLimiter.isAllowed("filip")).isTrue();
        }
    }

    private void recordFailures(String username, int times) {
        for (var i = 0; i < times; i++) {
            rateLimiter.recordFailure(username);
        }
    }
}
