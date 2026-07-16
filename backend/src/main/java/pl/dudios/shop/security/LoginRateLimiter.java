package pl.dudios.shop.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Prosty in-memory rate limiter logowania: okno przesuwne per znormalizowany
 * username. Porażki zapisujemy tylko przy złych credentialach; sukces zeruje
 * licznik. Stan żyje w pamięci procesu — po restarcie znika, co dla ochrony
 * przed brute-force na pojedynczej instancji w zupełności wystarcza.
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 15;
    private static final int WINDOW_SECONDS = 60;

    private final Map<String, Deque<Instant>> failures = new ConcurrentHashMap<>();

    public boolean isAllowed(String username) {
        Deque<Instant> attempts = failures.get(normalize(username));
        if (attempts == null) {
            return true;
        }
        prune(attempts);
        return attempts.size() < MAX_ATTEMPTS;
    }

    public void recordFailure(String username) {
        Deque<Instant> attempts = failures.computeIfAbsent(normalize(username), key -> new ConcurrentLinkedDeque<>());
        attempts.addLast(Instant.now());
        prune(attempts);
    }

    public void reset(String username) {
        failures.remove(normalize(username));
    }

    public void resetAll() {
        failures.clear();
    }

    private void prune(Deque<Instant> attempts) {
        Instant cutoff = Instant.now().minusSeconds(WINDOW_SECONDS);
        while (!attempts.isEmpty() && attempts.peekFirst().isBefore(cutoff)) {
            attempts.pollFirst();
        }
    }

    private String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

}
