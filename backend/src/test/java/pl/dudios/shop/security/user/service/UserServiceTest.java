package pl.dudios.shop.security.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.dudios.shop.security.user.controller.UserProfileUpdate;
import pl.dudios.shop.security.user.model.AppUser;
import pl.dudios.shop.security.user.model.AppUserDetails;
import pl.dudios.shop.security.user.model.Role;
import pl.dudios.shop.security.user.model.dto.ChangePassword;
import pl.dudios.shop.security.user.repository.UserRepo;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * Reguły obsługi konta użytkownika: logowanie (kontrakt UserDetailsService dla Spring Security),
 * zarządzanie zdjęciem profilowym i zmiana hasła.
 * <p>
 * Mockowane są wyłącznie granice systemu — repozytorium {@link UserRepo} (DB) oraz
 * {@link PasswordEncoder} (kryptografia). Encja {@link AppUser} i rekordy DTO to prawdziwe obiekty,
 * więc testy sprawdzają to, co widzi wołający: zwrócony {@link AppUserDetails}, zwrócony rekord,
 * rzucony wyjątek oraz stan encji zapisywany przez dirty checking wewnątrz transakcji.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepo userRepo;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("loading a user for authentication")
    class LoadingUserByUsername {

        @Test
        @DisplayName("hands Spring Security the stored username and password hash to verify the login against")
        void exposesUsernameAndPasswordHash() {
            //Given
            given(userRepo.findByUsername("jan@dudios.pl"))
                    .willReturn(Optional.of(enabledUser(7L, "jan@dudios.pl")));

            //When
            var details = userService.loadUserByUsername("jan@dudios.pl");

            //Then
            assertThat(details.getUsername()).isEqualTo("jan@dudios.pl");
            assertThat(details.getPassword()).isEqualTo("$2a$10$storedHash");
        }

        @Test
        @DisplayName("exposes the database id of the logged in user so endpoints can act on his own data")
        void exposesDatabaseId() {
            //Given
            given(userRepo.findByUsername("jan@dudios.pl"))
                    .willReturn(Optional.of(enabledUser(7L, "jan@dudios.pl")));

            //When
            var details = (AppUserDetails) userService.loadUserByUsername("jan@dudios.pl");

            //Then
            assertThat(details.getId()).isEqualTo(7L);
        }

        @Test
        @DisplayName("grants every role of the user as a ROLE_ prefixed authority so hasRole checks pass")
        void grantsRolesAsPrefixedAuthorities() {
            //Given
            var admin = user(1L, "admin@dudios.pl", true, List.of(Role.ROLE_USER, Role.ROLE_ADMIN));
            given(userRepo.findByUsername("admin@dudios.pl")).willReturn(Optional.of(admin));

            //When
            var details = userService.loadUserByUsername("admin@dudios.pl");

            //Then
            assertThat(details.getAuthorities())
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
        }

        @Test
        @DisplayName("reports a not yet activated account as disabled so the login is refused")
        void reportsNotActivatedAccountAsDisabled() {
            //Given
            var notActivated = user(8L, "nowy@dudios.pl", false, List.of(Role.ROLE_USER));
            given(userRepo.findByUsername("nowy@dudios.pl")).willReturn(Optional.of(notActivated));

            //When
            var details = userService.loadUserByUsername("nowy@dudios.pl");

            //Then
            assertThat(details.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("never locks or expires an account — activation is the only thing that gates the login")
        void neverLocksOrExpiresAccounts() {
            //Given
            given(userRepo.findByUsername("jan@dudios.pl"))
                    .willReturn(Optional.of(enabledUser(7L, "jan@dudios.pl")));

            //When
            var details = userService.loadUserByUsername("jan@dudios.pl");

            //Then
            assertThat(details.isEnabled()).isTrue();
            assertThat(details.isAccountNonLocked()).isTrue();
            assertThat(details.isAccountNonExpired()).isTrue();
            assertThat(details.isCredentialsNonExpired()).isTrue();
        }

        @Test
        @DisplayName("refuses to authenticate an unknown username and names it in the failure")
        void refusesUnknownUsername() {
            //Given
            given(userRepo.findByUsername("ghost@dudios.pl")).willReturn(Optional.empty());

            //When //Then
            assertThatThrownBy(() -> userService.loadUserByUsername("ghost@dudios.pl"))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("ghost@dudios.pl");
        }
    }

    @Nested
    @DisplayName("updating the profile image")
    class UpdatingProfileImage {

        @Test
        @DisplayName("replaces the previous image of the user and echoes the accepted one back")
        void replacesPreviousImage() {
            //Given
            var user = enabledUser(7L, "jan@dudios.pl");
            user.setImage("old-avatar.png");
            given(userRepo.findById(7L)).willReturn(Optional.of(user));

            //When
            var result = userService.updateProfileImage(7L, new UserProfileUpdate("new-avatar.png"));

            //Then
            assertThat(user.getImage()).isEqualTo("new-avatar.png");
            assertThat(result).isEqualTo(new UserProfileUpdate("new-avatar.png"));
        }

        @Test
        @DisplayName("lets the user drop his avatar by sending an empty image")
        void allowsRemovingTheAvatar() {
            //Given
            var user = enabledUser(7L, "jan@dudios.pl");
            user.setImage("old-avatar.png");
            given(userRepo.findById(7L)).willReturn(Optional.of(user));

            //When
            userService.updateProfileImage(7L, new UserProfileUpdate(null));

            //Then
            assertThat(user.getImage()).isNull();
        }

        @Test
        @DisplayName("refuses to update the image of a user that does not exist and persists nothing")
        void refusesUnknownUser() {
            //Given
            given(userRepo.findById(999L)).willReturn(Optional.empty());

            //When //Then
            assertThatThrownBy(() -> userService.updateProfileImage(999L, new UserProfileUpdate("avatar.png")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not found");
            then(userRepo).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("reading the profile image")
    class ReadingProfileImage {

        @Test
        @DisplayName("returns the image currently stored for the user")
        void returnsStoredImage() {
            //Given
            var user = enabledUser(7L, "jan@dudios.pl");
            user.setImage("avatar.png");
            given(userRepo.findById(7L)).willReturn(Optional.of(user));

            //When
            var result = userService.getProfileImage(7L);

            //Then
            assertThat(result.image()).isEqualTo("avatar.png");
        }

        @Test
        @DisplayName("answers with an empty image instead of failing when the user never uploaded one")
        void returnsEmptyImageWhenNoneUploaded() {
            //Given
            given(userRepo.findById(7L)).willReturn(Optional.of(enabledUser(7L, "jan@dudios.pl")));

            //When
            var result = userService.getProfileImage(7L);

            //Then
            assertThat(result.image()).isNull();
        }

        @Test
        @DisplayName("refuses to read the image of a user that does not exist")
        void refusesUnknownUser() {
            //Given
            given(userRepo.findById(999L)).willReturn(Optional.empty());

            //When //Then
            assertThatThrownBy(() -> userService.getProfileImage(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not found");
        }
    }

    @Nested
    @DisplayName("changing the password of a logged in user")
    class ChangingPassword {

        @Test
        @DisplayName("stores the new password only in its encoded form, never the plain text one")
        void storesOnlyTheEncodedPassword() {
            //Given
            var user = enabledUser(7L, "jan@dudios.pl");
            given(userRepo.findById(7L)).willReturn(Optional.of(user));
            given(passwordEncoder.encode("tajneHaslo123")).willReturn("$2a$10$newHash");

            //When
            userService.changePassword(7L, new ChangePassword("tajneHaslo123", "tajneHaslo123", null));

            //Then
            assertThat(user.getPassword()).isEqualTo("$2a$10$newHash");
            assertThat(user.getPassword()).isNotEqualTo("tajneHaslo123");
        }

        @Test
        @DisplayName("refuses to change the password of a user that does not exist and never encodes it")
        void refusesUnknownUser() {
            //Given
            given(userRepo.findById(999L)).willReturn(Optional.empty());

            //When //Then
            assertThatThrownBy(() -> userService.changePassword(999L, new ChangePassword("tajneHaslo123", "tajneHaslo123", null)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not found");
            then(passwordEncoder).shouldHaveNoInteractions();
            then(userRepo).should(never()).save(any());
        }
    }

    private static AppUser enabledUser(Long id, String username) {
        return user(id, username, true, List.of(Role.ROLE_USER));
    }

    private static AppUser user(Long id, String username, boolean enabled, List<Role> authorities) {
        return AppUser.builder()
                .id(id)
                .username(username)
                .password("$2a$10$storedHash")
                .enabled(enabled)
                .authorities(authorities)
                .build();
    }
}
