package pl.dudios.shop.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 401 dla wyjątków uwierzytelniania delegowanych tu przez
     * {@link pl.dudios.shop.security.DelegatedAuthEntryPoint} oraz złych
     * credentiali z endpointu logowania. Nawigacja przeglądarki (Accept:
     * text/html) na chronioną trasę SPA dostaje index.html — router Angulara
     * sam przekieruje na /login.
     */
    @ExceptionHandler(AuthenticationException.class)
    public Object handleAuthentication(AuthenticationException e,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        if (isSpaNavigation(request)) {
            // Handler jest wołany przez entry point poza DispatcherServletem
            // (resolveException) — zwróconego widoku nikt by nie wyrenderował,
            // więc forwardujemy dispatcherem wprost.
            try {
                request.getRequestDispatcher("/index.html").forward(request, response);
                return null;
            } catch (Exception forwardFailure) {
                // spadamy do odpowiedzi JSON poniżej
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", e.getMessage() == null ? "Unauthorized" : e.getMessage()));
    }

    @ResponseBody
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", e.getMessage() == null ? "Bad request" : e.getMessage()));
    }

    /**
     * Serwisy sygnalizują brak bytu przez {@code orElseThrow()} — dla klienta
     * to 404, a nie 500.
     */
    @ResponseBody
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNoSuchElement(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "Resource not found"));
    }

    /**
     * Deep-linki SPA: niezmapowane, bezrozszerzeniowe ścieżki (np. /category/fruits
     * po odświeżeniu strony) forwardujemy do index.html, żeby router Angulara
     * przejął nawigację — wzorzec z hercu-pulpit.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Object handleNoResource(HttpServletRequest request) {
        if (isSpaRoute(request.getRequestURI())) {
            return "forward:/index.html";
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "Resource not found"));
    }

    private static boolean isSpaNavigation(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("text/html") && isSpaRoute(request.getRequestURI());
    }

    private static boolean isSpaRoute(String uri) {
        return uri != null
                && !uri.startsWith("/api/")
                && !uri.startsWith("/actuator/")
                && !uri.startsWith("/data/")
                && !uri.contains(".");
    }

}
