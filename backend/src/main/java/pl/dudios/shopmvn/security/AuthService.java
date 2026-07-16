package pl.dudios.shopmvn.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dudios.shopmvn.security.model.AuthRequest;
import pl.dudios.shopmvn.security.model.RegisterRequest;
import pl.dudios.shopmvn.security.user.model.AppUser;
import pl.dudios.shopmvn.security.user.model.Role;
import pl.dudios.shopmvn.security.user.repository.UserRepo;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final LoginRateLimiter loginRateLimiter;

    public Authentication login(AuthRequest authRequest) {
        if (!loginRateLimiter.isAllowed(authRequest.username())) {
            log.error("Rate limit hit for username '{}'", authRequest.username());
            throw new TooManyAttemptsException("Too many failed login attempts. Please try again in a minute.");
        }

        Authentication authenticate;
        try {
            authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authRequest.username(),
                    authRequest.password())
            );
        } catch (BadCredentialsException e) {
            log.error("Bad credentials for username '{}'", authRequest.username());
            loginRateLimiter.recordFailure(authRequest.username());
            throw e;
        }

        loginRateLimiter.reset(authRequest.username());

        return authenticate;
    }

    @Transactional
    public Authentication register(RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        String username = request.username().toLowerCase().trim();
        if (userRepo.existsByUsername(username)) {
            throw new IllegalArgumentException("User already exists");
        }

        try {
            // saveAndFlush: unique constraint musi wybuchnąć TERAZ (w try),
            // a nie przy commicie transakcji — inaczej wyścig dwóch rejestracji
            // kończy się gołym 500 zamiast czytelnego błędu.
            userRepo.saveAndFlush(AppUser.builder()
                    .username(username)
                    .password(passwordEncoder.encode(request.password()))
                    .enabled(true)
                    .authorities(List.of(Role.ROLE_USER))
                    .build());
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("User already exists");
        }

        return login(new AuthRequest(username, request.password()));
    }

}
