package pl.dudios.shop.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.dudios.shop.security.model.AuthRequest;
import pl.dudios.shop.security.model.RegisterRequest;
import pl.dudios.shop.security.user.model.AppUser;
import pl.dudios.shop.security.user.model.Role;
import pl.dudios.shop.security.user.repository.UserRepo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * Reguły logowania i rejestracji konta. Mockowane są wyłącznie granice systemu:
 * repozytorium użytkowników (DB), AuthenticationManager i PasswordEncoder
 * (Spring Security) oraz LoginRateLimiter (stan współdzielony w pamięci procesu).
 * Encja AppUser, enum Role i rekordy AuthRequest/RegisterRequest to prawdziwe
 * obiekty, więc testy pilnują tego, co widzi wołający — zwróconej sesji,
 * rzuconego wyjątku i kształtu konta zapisanego w bazie — a nie kroków serwisu.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String USERNAME = "kowalski@example.pl";
    private static final String RAW_PASSWORD = "Sekret123";
    private static final String ENCODED_PASSWORD = "$2a$10$zaszyfrowaneHaslo";

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepo userRepo;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private LoginRateLimiter loginRateLimiter;
    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("logging in an existing user")
    class LoggingIn {

        @Test
        @DisplayName("hands the caller the session produced by the authentication manager")
        void returnsSessionFromAuthenticationManager() {
            //Given
            var session = authenticatedSession();
            given(loginRateLimiter.isAllowed(USERNAME)).willReturn(true);
            given(authenticationManager.authenticate(any())).willReturn(session);

            //When
            var result = authService.login(loginRequest());

            //Then
            assertThat(result).isSameAs(session);
        }

        @Test
        @DisplayName("clears the failure counter so a successful login does not count against the next one")
        void clearsFailureCounterAfterSuccess() {
            //Given
            given(loginRateLimiter.isAllowed(USERNAME)).willReturn(true);

            //When
            authService.login(loginRequest());

            //Then
            then(loginRateLimiter).should().reset(USERNAME);
            then(loginRateLimiter).should(never()).recordFailure(any());
        }
    }

    @Nested
    @DisplayName("refusing a login attempt")
    class RefusingLogin {

        @Test
        @DisplayName("blocks a rate-limited username without ever asking the authentication manager")
        void blocksRateLimitedUsername() {
            //Given
            given(loginRateLimiter.isAllowed(USERNAME)).willReturn(false);

            //When //Then
            assertThatThrownBy(() -> authService.login(loginRequest()))
                    .isInstanceOf(TooManyAttemptsException.class)
                    .hasMessageContaining("Too many failed login attempts");
            then(authenticationManager).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("lets a bad-credentials failure reach the caller instead of swallowing it")
        void propagatesBadCredentials() {
            //Given
            given(loginRateLimiter.isAllowed(USERNAME)).willReturn(true);
            given(authenticationManager.authenticate(any())).willThrow(new BadCredentialsException("Bad credentials"));

            //When //Then
            assertThatThrownBy(() -> authService.login(loginRequest()))
                    .isInstanceOf(BadCredentialsException.class);
        }

        @Test
        @DisplayName("counts a wrong password towards the rate limit and keeps the counter running")
        void countsWrongPasswordTowardsRateLimit() {
            //Given
            given(loginRateLimiter.isAllowed(USERNAME)).willReturn(true);
            given(authenticationManager.authenticate(any())).willThrow(new BadCredentialsException("Bad credentials"));

            //When
            assertThatThrownBy(() -> authService.login(loginRequest()));

            //Then
            then(loginRateLimiter).should().recordFailure(USERNAME);
            then(loginRateLimiter).should(never()).reset(any());
        }
    }

    @Nested
    @DisplayName("registering a brand new account")
    class RegisteringNewAccount {

        @BeforeEach
        void freeUsernameAndUnblockedLogin() {
            given(userRepo.existsByUsername(any())).willReturn(false);
            given(passwordEncoder.encode(RAW_PASSWORD)).willReturn(ENCODED_PASSWORD);
            given(loginRateLimiter.isAllowed(any())).willReturn(true);
        }

        @Test
        @DisplayName("stores the username lower-cased and trimmed so the same e-mail cannot be registered twice")
        void storesNormalisedUsername() {
            //Given
            var request = registerRequest("  KOWALSKI@Example.PL  ");

            //When
            authService.register(request);

            //Then
            assertThat(savedUser().getUsername()).isEqualTo(USERNAME);
        }

        @Test
        @DisplayName("never lets the raw password reach the database")
        void storesOnlyEncodedPassword() {
            //Given
            var request = registerRequest(USERNAME);

            //When
            authService.register(request);

            //Then
            assertThat(savedUser().getPassword())
                    .isEqualTo(ENCODED_PASSWORD)
                    .isNotEqualTo(RAW_PASSWORD);
        }

        @Test
        @DisplayName("creates an enabled account with the plain user role, never an admin one")
        void createsEnabledPlainUserAccount() {
            //Given
            var request = registerRequest(USERNAME);

            //When
            authService.register(request);

            //Then
            var saved = savedUser();
            assertThat(saved.isEnabled()).isTrue();
            assertThat(saved.getAuthorities()).containsExactly(Role.ROLE_USER);
        }

        @Test
        @DisplayName("signs the new user in with the raw password and the normalised username, so the account works right away")
        void signsNewUserInWithRawPassword() {
            //Given
            var request = registerRequest("  KOWALSKI@Example.PL  ");

            //When
            authService.register(request);

            //Then
            var credentials = ArgumentCaptor.forClass(Authentication.class);
            then(authenticationManager).should().authenticate(credentials.capture());
            assertThat(credentials.getValue().getPrincipal()).isEqualTo(USERNAME);
            assertThat(credentials.getValue().getCredentials()).isEqualTo(RAW_PASSWORD);
        }

        @Test
        @DisplayName("returns the session of the freshly created account")
        void returnsSessionOfNewAccount() {
            //Given
            var session = authenticatedSession();
            given(authenticationManager.authenticate(any())).willReturn(session);

            //When
            var result = authService.register(registerRequest(USERNAME));

            //Then
            assertThat(result).isSameAs(session);
        }

        private AppUser savedUser() {
            var user = ArgumentCaptor.forClass(AppUser.class);
            then(userRepo).should().saveAndFlush(user.capture());
            return user.getValue();
        }
    }

    @Nested
    @DisplayName("refusing a registration")
    class RefusingRegistration {

        @Test
        @DisplayName("rejects a typo in the password confirmation before touching the database")
        void rejectsMismatchedConfirmation() {
            //Given
            var request = new RegisterRequest(USERNAME, RAW_PASSWORD, "Sekret124");

            //When //Then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Passwords do not match");
            then(userRepo).shouldHaveNoInteractions();
            then(passwordEncoder).shouldHaveNoInteractions();
            then(authenticationManager).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("rejects an e-mail that already has an account instead of overwriting it")
        void rejectsAlreadyTakenUsername() {
            //Given
            given(userRepo.existsByUsername(USERNAME)).willReturn(true);

            //When //Then
            assertThatThrownBy(() -> authService.register(registerRequest(USERNAME)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User already exists");
            then(userRepo).should(never()).saveAndFlush(any(AppUser.class));
            then(authenticationManager).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("turns a lost race for the same e-mail into the same readable error, not a raw constraint failure")
        void turnsConstraintViolationIntoReadableError() {
            //Given
            given(userRepo.existsByUsername(USERNAME)).willReturn(false);
            given(userRepo.saveAndFlush(any(AppUser.class)))
                    .willThrow(new DataIntegrityViolationException("users_username_key"));

            //When //Then
            assertThatThrownBy(() -> authService.register(registerRequest(USERNAME)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User already exists");
            then(authenticationManager).shouldHaveNoInteractions();
        }
    }

    private static AuthRequest loginRequest() {
        return new AuthRequest(USERNAME, RAW_PASSWORD);
    }

    private static RegisterRequest registerRequest(String username) {
        return new RegisterRequest(username, RAW_PASSWORD, RAW_PASSWORD);
    }

    private static Authentication authenticatedSession() {
        return UsernamePasswordAuthenticationToken.authenticated(
                USERNAME,
                null,
                List.of(new SimpleGrantedAuthority(Role.ROLE_USER.name()))
        );
    }
}
