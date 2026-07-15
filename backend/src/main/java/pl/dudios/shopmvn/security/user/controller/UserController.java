package pl.dudios.shopmvn.security.user.controller;

import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.dudios.shopmvn.security.user.model.dto.ChangePassword;
import pl.dudios.shopmvn.security.user.service.UserService;

@AllArgsConstructor
@RestController
public class UserController {

    private final UserService userService;

    @PutMapping("/profile/image")
    public UserProfileUpdate updateProfileImage(@AuthenticationPrincipal Long userId, @RequestBody UserProfileUpdate userProfileUpdate) {
        return userService.updateProfileImage(userId, userProfileUpdate);
    }

    @GetMapping("/profile/{userId}/image")
    public UserProfileUpdate getProfileImage(@PathVariable Long userId) {
        System.out.println(userId);
        return userService.getProfileImage(userId);
    }

    @PostMapping("/profile/changePassword")
    public void changePassword(@AuthenticationPrincipal Long userId, @RequestBody ChangePassword changePassword) {
        userService.changePassword(userId, changePassword);
    }

}
