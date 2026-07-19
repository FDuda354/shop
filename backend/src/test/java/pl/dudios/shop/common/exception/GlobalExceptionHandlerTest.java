package pl.dudios.shop.common.exception;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Kontrakt odpowiedzi błędowych, który widzi klient sklepu: status HTTP, treść
 * ciała odpowiedzi oraz decyzja "JSON czy powłoka Angulara".
 * <p>
 * Handler nie ma żadnych współpracowników do wstrzyknięcia, więc nie ma tu ani
 * MockitoExtension, ani {@code @Mock} — jedyną granicą systemu jest kontener
 * serwletowy, a ten zastępują prawdziwe atrapy Springa
 * ({@link MockHttpServletRequest} / {@link MockHttpServletResponse}). Dzięki nim
 * forward do {@code /index.html} jest obserwowalny wprost na odpowiedzi, a testy
 * nie zależą od tego, które gettery requestu handler wywołał po drodze.
 * <p>
 * Wyjątki są prawdziwe ({@link BadCredentialsException}, {@link IllegalArgumentException},
 * {@link NoSuchElementException}) — nic domenowego nie jest mockowane.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Nested
    @DisplayName("failed authentication: SPA shell for browsers, 401 JSON for everyone else")
    class AuthenticationFailures {

        @Test
        @DisplayName("serves the Angular shell instead of a 401 when a browser navigates to a protected SPA route")
        void servesAngularShellForBrowserNavigation() {
            //Given
            var request = browserNavigation("/orders");
            var response = new MockHttpServletResponse();

            //When
            var result = handler.handleAuthentication(new BadCredentialsException("Full authentication is required"), request, response);

            //Then
            assertThat(response.getForwardedUrl()).isEqualTo("/index.html");
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("answers a REST client with 401 and the reason the authentication failed")
        void answersRestClientWithUnauthorized() {
            //Given
            var request = restCall("/api/orders");
            var response = new MockHttpServletResponse();

            //When
            var result = handler.handleAuthentication(new BadCredentialsException("Bad credentials"), request, response);

            //Then
            assertThat(result).isInstanceOf(ResponseEntity.class);
            var entity = (ResponseEntity<?>) result;
            assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(entity.getBody()).isEqualTo(Map.of("message", "Bad credentials"));
            assertThat(response.getForwardedUrl()).isNull();
        }

        @Test
        @DisplayName("answers with 401 JSON when the caller declares no Accept header at all")
        void answersWithJsonWhenAcceptHeaderIsMissing() {
            //Given
            var request = new MockHttpServletRequest("GET", "/orders");
            var response = new MockHttpServletResponse();

            //When
            var result = handler.handleAuthentication(new BadCredentialsException("Bad credentials"), request, response);

            //Then
            assertThat(result).isInstanceOf(ResponseEntity.class);
            var entity = (ResponseEntity<?>) result;
            assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getForwardedUrl()).isNull();
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(strings = {"/api/orders", "/actuator/health", "/data/export", "/main-A1B2C3.js"})
        @DisplayName("answers with 401 JSON when a browser hits a backend path or an asset, not a SPA route")
        void answersWithJsonForNonSpaRoutes(String uri) {
            //Given
            var request = browserNavigation(uri);
            var response = new MockHttpServletResponse();

            //When
            var result = handler.handleAuthentication(new BadCredentialsException("Bad credentials"), request, response);

            //Then
            assertThat(result).isInstanceOf(ResponseEntity.class);
            var entity = (ResponseEntity<?>) result;
            assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getForwardedUrl()).isNull();
        }

        @Test
        @DisplayName("falls back to a 401 JSON body when forwarding to the Angular shell fails")
        void fallsBackToJsonWhenForwardFails() {
            //Given
            var request = browserNavigationWithBrokenDispatcher("/orders");
            var response = new MockHttpServletResponse();

            //When
            var result = handler.handleAuthentication(new BadCredentialsException("Bad credentials"), request, response);

            //Then
            assertThat(result).isInstanceOf(ResponseEntity.class);
            var entity = (ResponseEntity<?>) result;
            assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(entity.getBody()).isEqualTo(Map.of("message", "Bad credentials"));
        }

        @Test
        @DisplayName("substitutes a generic message when the authentication failure carries none")
        void substitutesGenericMessageWhenExceptionHasNone() {
            //Given
            var request = restCall("/api/orders");
            var response = new MockHttpServletResponse();

            //When
            var result = handler.handleAuthentication(new BadCredentialsException(null), request, response);

            //Then
            assertThat(result).isInstanceOf(ResponseEntity.class);
            var entity = (ResponseEntity<?>) result;
            assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(entity.getBody()).isEqualTo(Map.of("message", "Unauthorized"));
        }
    }

    @Nested
    @DisplayName("invalid caller input is a 400, not a 500")
    class InvalidInput {

        @Test
        @DisplayName("returns 400 carrying the rejection reason so the caller can fix the request")
        void returnsBadRequestWithReason() {
            //Given
            var rejection = new IllegalArgumentException("Quantity must be greater than zero");

            //When
            var response = handler.handleIllegalArgument(rejection);

            //Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isEqualTo(Map.of("message", "Quantity must be greater than zero"));
        }

        @Test
        @DisplayName("substitutes a generic message when the rejection carries none")
        void substitutesGenericMessageWhenRejectionHasNone() {
            //Given
            var rejection = new IllegalArgumentException((String) null);

            //When
            var response = handler.handleIllegalArgument(rejection);

            //Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isEqualTo(Map.of("message", "Bad request"));
        }
    }

    @Nested
    @DisplayName("a missing domain object is a 404, not a 500")
    class MissingDomainObject {

        @Test
        @DisplayName("returns 404 without leaking the internal lookup details to the client")
        void returnsNotFoundWithoutLeakingInternals() {
            //Given
            var lookupFailure = new NoSuchElementException("No value present for productRepo.findById(4242)");

            //When
            var response = handler.handleNoSuchElement(lookupFailure);

            //Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isEqualTo(Map.of("message", "Resource not found"));
            assertThat(response.getBody().get("message")).doesNotContain("4242");
        }
    }

    @Nested
    @DisplayName("unmapped paths: SPA deep links reach the Angular router, everything else 404s")
    class UnmappedPaths {

        @Test
        @DisplayName("forwards a refreshed SPA deep link to the Angular shell so the router can take over")
        void forwardsSpaDeepLinkToAngularShell() {
            //Given
            var request = new MockHttpServletRequest("GET", "/category/fruits");

            //When
            var result = handler.handleNoResource(request);

            //Then
            assertThat(result).isEqualTo("forward:/index.html");
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(strings = {"/api/orders", "/actuator/health", "/data/export", "/assets/missing-logo.png"})
        @DisplayName("returns 404 for unmapped backend paths and missing assets instead of the Angular shell")
        void returnsNotFoundForBackendPathsAndAssets(String uri) {
            //Given
            var request = new MockHttpServletRequest("GET", uri);

            //When
            var result = handler.handleNoResource(request);

            //Then
            assertThat(result).isInstanceOf(ResponseEntity.class);
            var entity = (ResponseEntity<?>) result;
            assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(entity.getBody()).isEqualTo(Map.of("message", "Resource not found"));
        }
    }

    private static MockHttpServletRequest browserNavigation(String uri) {
        var request = new MockHttpServletRequest("GET", uri);
        request.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        return request;
    }

    private static MockHttpServletRequest restCall(String uri) {
        var request = new MockHttpServletRequest("GET", uri);
        request.addHeader("Accept", "application/json");
        return request;
    }

    private static MockHttpServletRequest browserNavigationWithBrokenDispatcher(String uri) {
        var request = new MockHttpServletRequest("GET", uri) {
            @Override
            public RequestDispatcher getRequestDispatcher(String path) {
                return new RequestDispatcher() {
                    @Override
                    public void forward(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException {
                        throw new ServletException("index.html is not on the static resources path");
                    }

                    @Override
                    public void include(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException {
                        throw new ServletException("not used by the handler under test");
                    }
                };
            }
        };
        request.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        return request;
    }
}
