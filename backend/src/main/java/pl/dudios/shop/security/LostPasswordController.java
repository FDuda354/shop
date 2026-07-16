package pl.dudios.shop.security;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.dudios.shop.security.user.model.dto.ChangePassword;
import pl.dudios.shop.security.user.model.dto.EmailObject;
import pl.dudios.shop.security.user.service.LostPasswordService;

@RestController
@RequiredArgsConstructor
public class LostPasswordController {

    private final LostPasswordService lostPasswordService;

    @PostMapping("/lostPassword")
    public void lostPassword(@RequestBody EmailObject email) {
        lostPasswordService.sendLostPasswordLink(email);
    }

    @PostMapping("/changePassword")
    public void changePassword(@RequestBody ChangePassword changePassword) {
        lostPasswordService.changePassword(changePassword);
    }
}
