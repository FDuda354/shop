package pl.dudios.shop.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.dudios.shop.security.model.AuthRequest;
import pl.dudios.shop.security.model.RegisterRequest;
import pl.dudios.shop.security.user.model.AppUserDetails;
import pl.dudios.shop.security.user.model.dto.UserDto;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SecurityContextRepository securityContextRepository;

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@Valid @RequestBody AuthRequest authRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        Authentication authentication = authService.login(new AuthRequest(
                authRequest.username().toLowerCase().trim(),
                authRequest.password())
        );

        establishSession(authentication, request, response);

        return ResponseEntity.ok(UserDto.from((AppUserDetails) authentication.getPrincipal()));
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest registerRequest,
                                            HttpServletRequest request,
                                            HttpServletResponse response) {
        Authentication authentication = authService.register(registerRequest);

        establishSession(authentication, request, response);

        return ResponseEntity.ok(UserDto.from((AppUserDetails) authentication.getPrincipal()));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal AppUserDetails user) {
        return ResponseEntity.ok(UserDto.from(user));
    }

    private void establishSession(Authentication authentication,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        // Ochrona przed session fixation — świeże ID sesji po uwierzytelnieniu.
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }

}
