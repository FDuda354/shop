package pl.dudios.shopmvn.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.dudios.shopmvn.security.user.model.AppUser;
import pl.dudios.shopmvn.security.user.model.AppUserDetails;
import pl.dudios.shopmvn.security.user.model.Role;
import pl.dudios.shopmvn.security.user.repository.UserRepo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;

@RestController
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final UserRepo userRepo;

    private final Long expirationTime;
    private final String secret;

    public LoginController(AuthenticationManager authenticationManager,
                           UserRepo userRepo,
                           @Value("${jwt.expirationTime}") Long expirationTime,
                           @Value("${jwt.secret}") String secret) {

        this.authenticationManager = authenticationManager;
        this.userRepo = userRepo;
        this.expirationTime = expirationTime;
        this.secret = secret;
    }

    @PostMapping("/login")
    public RoLResponse login(@RequestBody @Valid LoginCredentials credentials) {
        return auth(credentials.username(), credentials.password());
    }


    @PostMapping("/register")
    public RoLResponse register(@RequestBody @Valid RegisterCredentials credentials) {
        if (!credentials.password().equals(credentials.confirmPassword()))
            throw new IllegalArgumentException("Passwords do not match");
        if (userRepo.existsByUsername(credentials.username()))
            throw new IllegalArgumentException("User already exists");

        userRepo.save(AppUser.builder()
                .username(credentials.username())
                .password("{bcrypt}" + new BCryptPasswordEncoder().encode(credentials.password()))
                .enabled(true)
                .authorities(List.of(Role.ROLE_USER))
                .build());
        return auth(credentials.username(), credentials.password());
    }

    private RoLResponse auth(String username, String password) {
        AppUser user = userRepo.findByUsername(username).orElseThrow();
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getId(), password));

        AppUserDetails principal = (AppUserDetails) authentication.getPrincipal();

        String token = JWT.create()
                .withSubject(String.valueOf(principal.getId()))
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationTime))
                .sign(Algorithm.HMAC256(secret.getBytes()));

        return new RoLResponse(token, principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Role.ROLE_ADMIN.name())));

    }

    private record RoLResponse(String token, boolean isAdmin) {
    }

    private record LoginCredentials(@NotBlank String username,
                                    @NotBlank String password) {

    }

    private record RegisterCredentials(@Email String username,
                                       @NotBlank String password,
                                       @NotBlank String confirmPassword) {

    }

}
