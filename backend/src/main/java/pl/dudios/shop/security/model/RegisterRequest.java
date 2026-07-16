package pl.dudios.shop.security.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "E-mail is required")
        @Email(message = "Invalid e-mail")
        // Limit zgodny z users.username varchar(50) — dłuższy e-mail wybuchłby
        // dopiero na constraincie bazy jako 500.
        @Size(max = 50, message = "E-mail is too long")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 200, message = "Password must be between 8 and 200 characters")
        String password,

        @NotBlank(message = "Password confirmation is required")
        String confirmPassword
) {

}
