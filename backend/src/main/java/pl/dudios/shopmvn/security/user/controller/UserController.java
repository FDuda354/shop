package pl.dudios.shopmvn.security.user.controller;

import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.dudios.shopmvn.security.user.model.AppUserDetails;
import pl.dudios.shopmvn.security.user.model.dto.ChangePassword;
import pl.dudios.shopmvn.security.user.service.UserService;

@AllArgsConstructor
@RestController
public class UserController {

    private final UserService userService;

    @PutMapping("/profile/image")
    public UserProfileUpdate updateProfileImage(@AuthenticationPrincipal AppUserDetails user, @RequestBody UserProfileUpdate userProfileUpdate) {
        return userService.updateProfileImage(requireUserId(user), userProfileUpdate);
    }

    @GetMapping("/profile/{userId}/image")
    public UserProfileUpdate getProfileImage(@PathVariable Long userId) {
        return userService.getProfileImage(userId);
    }

    @PostMapping("/profile/changePassword")
    public void changePassword(@AuthenticationPrincipal AppUserDetails user, @RequestBody ChangePassword changePassword) {
        userService.changePassword(requireUserId(user), changePassword);
    }

    private static Long requireUserId(AppUserDetails user) {
        if (user == null) {
            throw new IllegalArgumentException("Not logged in");
        }
        return user.getId();
    }

}
