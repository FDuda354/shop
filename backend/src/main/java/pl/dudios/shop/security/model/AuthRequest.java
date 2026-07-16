package pl.dudios.shop.security.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequest(
        @NotBlank(message = "Username is required")
        @Size(max = 150, message = "Username is too long")
        String username,

        @NotBlank(message = "Password is required")
        @Size(max = 200, message = "Password is too long")
        String password
) {

}
