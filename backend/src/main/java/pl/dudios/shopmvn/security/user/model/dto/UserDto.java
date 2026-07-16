package pl.dudios.shopmvn.security.user.model.dto;

import pl.dudios.shopmvn.security.user.model.AppUserDetails;
import pl.dudios.shopmvn.security.user.model.Role;

public record UserDto(Long id, String username, boolean admin) {

    public static UserDto from(AppUserDetails user) {
        boolean admin = user.getAuthorities().stream()
                .anyMatch(a -> Role.ROLE_ADMIN.name().equals(a.getAuthority()));
        return new UserDto(user.getId(), user.getUsername(), admin);
    }

}
