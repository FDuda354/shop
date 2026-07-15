package pl.dudios.shopmvn.security.user.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dudios.shopmvn.common.mail.EmailClientService;
import pl.dudios.shopmvn.security.user.model.AppUser;
import pl.dudios.shopmvn.security.user.model.dto.ChangePassword;
import pl.dudios.shopmvn.security.user.model.dto.EmailObject;
import pl.dudios.shopmvn.security.user.repository.UserRepo;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LostPasswordService {

    private final UserRepo userRepo;
    private final EmailClientService emailClientService;

    @Value("${app.serviceAddress}")
    private String baseAddress;

    @Transactional
    public void sendLostPasswordLink(EmailObject email) {
        AppUser user = userRepo.findByUsername(email.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String hash = generateHashForLostPassword(user);

        user.setHash(hash);
        user.setHashDate(LocalDateTime.now());

        emailClientService.getSender().sendEmail(email.email(), "Reset password", createMessage(createLink(hash)));

    }

    private String createMessage(String hashLink) {
        System.out.println("===================================");
        System.out.println(hashLink);
        return "Link to change your password" +
                "\n\nClick: " +
                "\n" + hashLink +
                "\n\nDudios";
    }

    private String createLink(String hash) {
        return baseAddress + "/lostPassword/" + hash;
    }

    private String generateHashForLostPassword(AppUser user) {
        String toHash = user.getId() + user.getUsername() + user.getPassword() + LocalDateTime.now();
        return DigestUtils.sha256Hex(toHash);
    }

    @Transactional
    public void changePassword(ChangePassword changePassword) {
        if (!Objects.equals(changePassword.password(), changePassword.repeatPassword())) {
            throw new RuntimeException("Hasła nie są takie same");
        }
        AppUser user = userRepo.findByHash(changePassword.hash())
                .orElseThrow(() -> new RuntimeException("Invalid link"));
        if (user.getHashDate().plusMinutes(10).isAfter(LocalDateTime.now())) {
            user.setPassword("{bcrypt}" + new BCryptPasswordEncoder().encode(changePassword.password()));
            user.setHash(null);
            user.setHashDate(null);
        } else {
            throw new RuntimeException("Link expired");
        }

    }
}
