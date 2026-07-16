package pl.dudios.shop.security.user.model.dto;

public record ChangePassword(String password, String repeatPassword, String hash) {
}
